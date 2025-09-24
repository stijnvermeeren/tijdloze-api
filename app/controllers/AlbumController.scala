package controllers

import javax.inject._
import model.{AlbumCrawlField, AlbumId}
import model.api.{Album, AlbumSave}
import model.db.dao.{AlbumDAO, ArtistDAO}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.coverartarchive.CoverArtArchiveAPI
import util.crawl.{AutoIfUnique, CrawlHelper}
import util.currentlist.CurrentListUtil
import util.musicbrainz.{MusicbrainzAPI, MusicbrainzCrawler}
import util.wikidata.WikidataCrawler
import util.wikipedia.WikipediaAPI

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AlbumController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  dataCache: DataCache,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  coverArtArchiveAPI: CoverArtArchiveAPI,
  currentList: CurrentListUtil,
  wikipediaAPI: WikipediaAPI,
  musicbrainzCrawler: MusicbrainzCrawler,
  wikidataCrawler: WikidataCrawler
)(implicit ec: ExecutionContext) extends InjectedController {

  def get(albumId: AlbumId) = {
    Action.async { implicit rs =>
      dataCache.AlbumDataCache.load(albumId)
    }
  }

  def getByMusicbrainzId(musicbrainzId: String) = {
    Action.async { implicit rs =>
      for {
        album <- albumDAO.getByMusicbrainzId(musicbrainzId)
      } yield Ok(Json.toJson(Album.fromDb(album)))
    }
  }

  def post() = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[AlbumSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        albumSave => {
          for {
            newAlbumId <- albumDAO.create(albumSave)
            newAlbum <- albumDAO.get(newAlbumId)
            _ <- dataCache.reloadAlbum(newAlbumId)
          } yield postUpdate(newAlbum)
        }
      )
    }
  }

  def put(albumId: AlbumId) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[AlbumSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        albumSave => {
          for {
            _ <- albumDAO.update(albumId, albumSave)
            album <- albumDAO.get(albumId)
            _ <- dataCache.reloadAlbum(albumId)
          } yield postUpdate(album)
        }
      )
    }
  }

  private def postUpdate(album: model.db.Album) = {
    // don't block, but TODO log error
    album.urlWikiEn.foreach(wikipediaAPI.reload)
    album.urlWikiNl.foreach(wikipediaAPI.reload)

    for {
      artist <- artistDAO.get(album.artistId)
      _ <- musicbrainzCrawler.crawlAlbumDetails(album, artist)
      updatedAlbum <- albumDAO.get(album.id)
      _ <- wikidataCrawler.crawlAlbumDetails(updatedAlbum)
    } yield ()

    currentList.updateAlbum(Album.fromDb(album))
    checkMusicbrainz(album)
    Ok(Json.toJson(Album.fromDb(album)))
  }

  private def checkMusicbrainz(album: model.db.Album): Future[Unit] = {
    albumDAO.get(album.id).map(_.musicbrainzId) flatMap {
      case Some(musicbrainzId) =>
        coverArtArchiveAPI.searchAlbum(musicbrainzId) flatMap { cover =>
          albumDAO.setCover(album.id, cover)
        }
      case None =>
        Future.successful(())
    } flatMap { _ =>
      for {
        album <- albumDAO.get(album.id)
        _ <- dataCache.reloadAlbum(album.id)
      } yield {
        currentList.updateAlbum(Album.fromDb(album))
      }
    }
  }

  def delete(albumId: AlbumId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      for {
        _ <- albumDAO.delete(albumId)
        _ <- dataCache.CoreDataCache.reload()
      } yield {
        dataCache.AlbumDataCache.remove(albumId)
        currentList.deleteAlbum(albumId)
        Ok("")
      }
    }
  }
}
