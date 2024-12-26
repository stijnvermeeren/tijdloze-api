package controllers

import model.{AlbumId, ArtistId, SongId}
import model.api.{Album, Artist, CoreAlbum, CoreArtist, CoreData, CoreList, CoreSong, Song, Text}
import model.db.dao.{AlbumDAO, ArtistDAO, ListEntryDAO, ListExitDAO, SongDAO, TextDAO, YearDAO}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Ok

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.concurrent
import scala.util.{Failure, Success}

@Singleton
class DataCache @Inject() (
                            albumDAO: AlbumDAO,
                            artistDAO: ArtistDAO,
                            songDAO: SongDAO,
                            listEntryDAO: ListEntryDAO,
                            listExitDAO: ListExitDAO,
                            yearDAO: YearDAO,
                            textDAO: TextDAO
)(implicit val executionContext: ExecutionContext) {

  private val logger = Logger(getClass)

  abstract class Cache[T](key: T => String) {
    protected val cacheMap = concurrent.TrieMap[String, Future[Result]]()

    protected def loadData(id: T): Future[Result]

    def removeAll(): Unit = {
      cacheMap.clear()
    }

    def remove(id: T): Unit = {
      cacheMap.remove(key(id))
    }

    def load(id: T): Future[Result] = {
      cacheMap.getOrElse(key(id), reload(id))
    }

    def reload(id: T): Future[Result] = {
      val dataFuture = loadData(id)
      cacheMap.get(key(id)) match {
        case Some(_) =>
          // Already loaded: only replace value on success
          dataFuture onComplete {
            case Success(result) =>
              cacheMap.put(key(id), Future.successful(result))
            case Failure(e) =>
              logger.error(s"Failure when loading data for ${key(id)}", e)
          }
        case None =>
          // Not loaded yet: immediately set value to avoid parallel initialisation, remove again on error
          cacheMap.put(key(id), dataFuture)
          dataFuture onComplete {
            case Failure(e) =>
              logger.error(s"Failure when loading data for ${key(id)}", e)
              cacheMap.remove(key(id))
            case Success(_) =>

          }
      }

      dataFuture
    }
  }

  abstract class SingleEntryCache(key: String) extends Cache[Unit](_ => key) {
    protected def loadData(): Future[Result]

    protected def loadData(id: Unit): Future[Result] = {
      loadData()
    }

    def load(): Future[Result] = {
      load(())
    }

    def reload(): Future[Result] = {
      reload(())
    }
  }

  object CoreDataCache extends SingleEntryCache("core-data") {
    protected def loadData(): Future[Result] = {
      for {
        artists <- artistDAO.getAll()
        albums <- albumDAO.getAll()
        songs <- songDAO.getAll()
        entries <- listEntryDAO.getAll()
        years <- yearDAO.getAll()
        exits <- years.lastOption match {
          case Some(maxYear) => listExitDAO.getByYear(maxYear)
          case None => Future.successful(Seq.empty)
        }
      } yield {
        val songGroupedEntries = entries.groupBy(_.songId)
        val yearGroupedEntries = entries.groupBy(_.year).toSeq.map {
          case (year, values) =>
            val maxPosition = values.map(_.position).max
            val grouped = values.groupBy(_.position)
            val songIds = (1 to maxPosition).map(position =>
              grouped.get(position).flatMap(_.headOption.map(_.songId))
            )
            CoreList(
              year = year,
              songIds = songIds
            )
        }

        Ok(Json.toJson(CoreData(
          artists = artists.map(CoreArtist.fromDb),
          albums = albums.map(CoreAlbum.fromDb),
          songs = songs map { song =>
            CoreSong.fromDb(song, songGroupedEntries.getOrElse(song.id, Seq.empty))
          },
          years = years,
          lists = yearGroupedEntries,
          exitSongIds = exits.map(_.songId)
        )))
      }
    }
  }

  object ArtistDataCache extends Cache[ArtistId](artistId => s"artist/${artistId.value}") {
    protected def loadData(artistId: ArtistId): Future[Result] = {
      for {
        artist <- artistDAO.get(artistId)
      } yield Ok(Json.toJson(Artist.fromDb(artist)))
    }
  }

  object AlbumDataCache extends Cache[AlbumId](albumId => s"album/${albumId.value}") {
    protected def loadData(albumId: AlbumId): Future[Result] = {
      for {
        album <- albumDAO.get(albumId)
      } yield Ok(Json.toJson(Album.fromDb(album)))
    }
  }

  object SongDataCache extends Cache[SongId](songId => s"song/${songId.value}") {
    protected def loadData(songId: SongId): Future[Result] = {
      for {
        song <- songDAO.get(songId)
      } yield Ok(Json.toJson(Song.fromDb(song)))
    }
  }

  object TextCache extends Cache[String](key => s"text/$key") {
    protected def loadData(key: String): Future[Result] = {
      textDAO.get(key) map {
        case Some(text) =>
          Ok(Json.toJson(Text.fromDb(text)))
        case None =>
          Ok(Json.toJson(Text(key = key, value = "")))
      }
    }
  }

  def reloadArtist(artistId: ArtistId): Future[Unit] = {
    Future.sequence(Seq(
      CoreDataCache.reload(),
      ArtistDataCache.reload(artistId)
    )).map(_ => ())
  }

  def reloadAlbum(albumId: AlbumId): Future[Unit] = {
    Future.sequence(Seq(
      CoreDataCache.reload(),
      AlbumDataCache.reload(albumId)
    )).map(_ => ())
  }

  def reloadSong(songId: SongId): Future[Unit] = {
    Future.sequence(Seq(
      CoreDataCache.reload(),
      SongDataCache.reload(songId)
    )).map(_ => ())
  }
}
