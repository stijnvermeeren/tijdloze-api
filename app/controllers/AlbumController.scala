package controllers

import javax.inject._
import model.AlbumId
import model.api.{Album, AlbumSave}
import model.db.dao.AlbumDAO
import play.api.cache.{AsyncCacheApi, Cached}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AlbumController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  cache: AsyncCacheApi,
  cached: Cached,
  albumDAO: AlbumDAO
) extends InjectedController {

  def get(albumId: AlbumId) = {
    cached(s"album/${albumId.value}") {
      Action.async { implicit rs =>
        for {
          album <- albumDAO.get(albumId)
        } yield Ok(Json.toJson(Album.fromDb(album)))
      }
    }
  }

  def post() = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[AlbumSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        songSave => {
          for {
            newSong <- albumDAO.create(songSave)
          } yield {
            cache.remove("coreData")
            Ok(Json.toJson(Album.fromDb(newSong)))
          }
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
        songSave => {
          for {
            _ <- albumDAO.update(albumId, songSave)
            song <- albumDAO.get(albumId)
          } yield {
            cache.remove("coreData")
            cache.remove(s"album/${albumId.value}")
            Ok(Json.toJson(Album.fromDb(song)))
          }
        }
      )
    }
  }
}
