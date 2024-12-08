package controllers

import javax.inject._
import model.ArtistId
import model.api.{Artist, ArtistSave}
import model.db.dao.ArtistDAO
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ArtistController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  dataCache: DataCache,
  artistDAO: ArtistDAO,
  currentList: CurrentListUtil
) extends InjectedController {
  def get(artistId: ArtistId) = {
    Action.async { implicit rs =>
      dataCache.ArtistDataCache.load(artistId)
    }
  }

  def getByMusicbrainzId(musicbrainzId: String) = {
    Action.async { implicit rs =>
      for {
        artist <- artistDAO.getByMusicbrainzId(musicbrainzId)
      } yield Ok(Json.toJson(Artist.fromDb(artist)))
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
          } yield {
            dataCache.CoreDataCache.reload()
            dataCache.ArtistDataCache.reload(newArtistId)
            currentList.updateArtist(Artist.fromDb(newArtist))
            Ok(Json.toJson(Artist.fromDb(newArtist)))
          }
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
          } yield {
            dataCache.CoreDataCache.reload()
            dataCache.ArtistDataCache.reload(artistId)
            currentList.updateArtist(Artist.fromDb(artist))
            Ok(Json.toJson(Artist.fromDb(artist)))
          }
        }
      )
    }
  }

  def delete(artistId: ArtistId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      for {
        _ <- artistDAO.delete(artistId)
      } yield {
        dataCache.CoreDataCache.reload()
        dataCache.ArtistDataCache.remove(artistId)
        currentList.deleteArtist(artistId)
        Ok("")
      }
    }
  }
}
