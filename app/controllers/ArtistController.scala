package controllers

import javax.inject._
import model.ArtistId
import model.api.{Artist, ArtistSave}
import model.db.dao.ArtistDAO
import play.api.cache.{AsyncCacheApi, Cached}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ArtistController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  cache: AsyncCacheApi,
  cached: Cached,
  artistDAO: ArtistDAO,
  currentList: CurrentListUtil
) extends InjectedController {
  def get(artistId: ArtistId) = {
    cached(s"artist/${artistId.value}") {
      Action.async { implicit rs =>
        for {
          artist <- artistDAO.get(artistId)
        } yield Ok(Json.toJson(Artist.fromDb(artist)))
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
          } yield {
            cache.remove("coreData")
            currentList.refresh()
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
            cache.remove("coreData")
            currentList.refresh()
            cache.remove(s"artist/${artistId.value}")
            Ok(Json.toJson(Artist.fromDb(artist)))
          }
        }
      )
    }
  }
}
