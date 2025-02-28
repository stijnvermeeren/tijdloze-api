package controllers

import javax.inject._
import model.ArtistId
import model.api.{Artist, ArtistSave}
import model.db.dao.ArtistDAO
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.currentlist.CurrentListUtil
import util.musicbrainz.MusicbrainzCrawler
import util.wikidata.WikidataCrawler
import util.wikipedia.WikipediaAPI

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArtistController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  dataCache: DataCache,
  artistDAO: ArtistDAO,
  currentList: CurrentListUtil,
  wikipediaAPI: WikipediaAPI,
  musicbrainzCrawler: MusicbrainzCrawler,
  wikidataCrawler: WikidataCrawler
)(implicit ec: ExecutionContext) extends InjectedController {
  def get(artistId: ArtistId) = {
    Action.async { implicit rs =>
      dataCache.ArtistDataCache.load(artistId)
    }
  }

  def getByMusicbrainzId(musicbrainzId: String) = {
    Action.async { implicit rs =>
      for {
        artistOption <- artistDAO.getByMusicbrainzId(musicbrainzId)
      } yield {
        artistOption match {
          case Some(artist) => Ok(Json.toJson(Artist.fromDb(artist)))
          case None => NotFound
        }
      }
    }
  }

  def post() = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[ArtistSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        artistSave => {
          for {
            newArtistId <- artistDAO.create(artistSave)
            newArtist <- artistDAO.get(newArtistId)
            _ <- dataCache.reloadArtist(newArtistId)
          } yield postUpdate(newArtist)
        }
      )
    }
  }

  def put(artistId: ArtistId) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[ArtistSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        artistSave => {
          for {
            _ <- artistDAO.update(artistId, artistSave)
            artist <- artistDAO.get(artistId)
            _ <- dataCache.reloadArtist(artistId)
          } yield postUpdate(artist)
        }
      )
    }
  }

  private def postUpdate(artist: model.db.Artist) = {
    artist.urlWikiEn.foreach(wikipediaAPI.reload)
    artist.urlWikiNl.foreach(wikipediaAPI.reload)

    for {
      _ <- musicbrainzCrawler.crawlArtistDetails(artist)
      updatedArtist <- artistDAO.get(artist.id)
      _ <- wikidataCrawler.crawlArtistDetails(updatedArtist)
    } yield ()

    currentList.updateArtist(Artist.fromDb(artist))
    Ok(Json.toJson(Artist.fromDb(artist)))
  }


  def delete(artistId: ArtistId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      for {
        _ <- artistDAO.delete(artistId)
        _ <- dataCache.CoreDataCache.reload()
      } yield {
        dataCache.ArtistDataCache.remove(artistId)
        currentList.deleteArtist(artistId)
        Ok("")
      }
    }
  }
}
