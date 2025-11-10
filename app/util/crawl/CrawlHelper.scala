package util.crawl

import controllers.DataCache
import model.db.{Album, Artist, Song}
import model.{AlbumCrawlField, ArtistCrawlField, CrawlField, SongCrawlField}
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlAlbumDAO, CrawlArtistDAO, CrawlSongDAO, SongDAO}
import play.api.mvc.Result
import util.FutureUtil

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CrawlHelper @Inject()(
                             crawlArtistDAO: CrawlArtistDAO,
                             artistDAO: ArtistDAO,
                             albumDAO: AlbumDAO,
                             songDAO: SongDAO,
                             crawlAlbumDAO: CrawlAlbumDAO,
                             crawlSongDAO: CrawlSongDAO,
                             dataCache: DataCache
                           ) {
  def processAlbum(
    album: Album,
    field: AlbumCrawlField,
    candidateValues: Seq[String],
    comment: String,
    strategy: Strategy
  ): Future[Unit] = {
    process(
      album,
      field,
      candidateValues,
      strategy,
      albumDAO,
      cacheReload = () => dataCache.AlbumDataCache.reload(album.id),
      saveAuto = (albumId, value) => crawlAlbumDAO.saveAuto(
        albumId,
        field = field,
        value = Some(value),
        comment = Some(comment),
        isAccepted = true
      ),
      savePending = (albumId, value) => crawlAlbumDAO.savePending(
        albumId,
        field = field,
        value = Some(value),
        comment = Some(comment)
      )
    )
  }

  def processArtist(
    artist: Artist,
    field: ArtistCrawlField,
    candidateValues: Seq[String],
    comment: String,
    strategy: Strategy
  ): Future[Unit] = {
    process(
      artist,
      field,
      candidateValues,
      strategy,
      artistDAO,
      cacheReload = () => dataCache.ArtistDataCache.reload(artist.id),
      saveAuto = (artistId, value) => crawlArtistDAO.saveAuto(
        artistId,
        field = field,
        value = Some(value),
        comment = Some(comment),
        isAccepted = true
      ),
      savePending = (artistId, value) => crawlArtistDAO.savePending(
        artistId,
        field = field,
        value = Some(value),
        comment = Some(comment)
      )
    )
  }

  def processSong(
    song: Song,
    field: SongCrawlField,
    candidateValues: Seq[String],
    comment: String,
    strategy: Strategy
  ): Future[Unit] = {
    process(
      song,
      field,
      candidateValues,
      strategy,
      songDAO,
      cacheReload = () => dataCache.SongDataCache.reload(song.id),
      saveAuto = (songId, value) => crawlSongDAO.saveAuto(
        songId,
        field = field,
        value = Some(value),
        comment = Some(comment),
        isAccepted = true
      ),
      savePending = (songId, value) => crawlSongDAO.savePending(
        songId,
        field = field,
        value = Some(value),
        comment = Some(comment)
      )
    )
  }

  private def process[Model <: {val id: Id}, Id, DAO](
                                                       model: Model,
                                                       field: CrawlField[Model, Id, DAO],
                                                       candidateValues: Seq[String],
                                                       strategy: Strategy,
                                                       dao: DAO,
                                                       cacheReload: () => Future[Result],
                                                       saveAuto: (Id, String) => Future[Int],
                                                       savePending: (Id, String) => Future[Int]
  ): Future[Unit] = {

    val situation = if (candidateValues.length == 1) UniqueValue else NonUniqueValue

    FutureUtil.traverseSequentially(candidateValues) { candidateValue =>
      val compareWithExisting = field.extractValue(model) match {
        case Some(value) if field.equalsExisting(candidateValue, value) => EqualsExistingValue
        case Some(_) => NotEqualsExistingValue
        case None => NoExistingValue
      }

      strategy.decide(situation, compareWithExisting) match {
        case AutoAccept =>
          saveAuto(model.id, candidateValue) flatMap { _ =>
            field.save(dao)(model.id, Some(candidateValue))
          } map { _ =>
            cacheReload()
          }
        case Pending =>
          savePending(model.id, candidateValue)
      }
    }.map(_ => ())
  }
}
