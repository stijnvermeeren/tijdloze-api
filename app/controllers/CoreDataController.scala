package controllers

import javax.inject._
import model.api._
import model.db.dao._
import play.api.cache.Cached
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CoreDataController @Inject()(
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  songDAO: SongDAO,
  listEntryDAO: ListEntryDAO,
  listExitDAO: ListExitDAO,
  yearDAO: YearDAO,
  cached: Cached
) extends InjectedController {

  def get() = cached("coreData") {
    Action.async { implicit rs =>
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
          case (year, values) => CoreList(
            year = year,
            songIds = values.sortBy(_.position).map(_.songId),
            top100SongCount = values.count(_.position <= 100)
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
}
