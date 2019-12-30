package controllers

import javax.inject._
import model.AlbumId
import model.api.{Album, AlbumSave}
import model.db.dao.AlbumDAO
import play.api.cache.{AsyncCacheApi, Cached}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AlbumController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  cache: AsyncCacheApi,
  cached: Cached,
  albumDAO: AlbumDAO,
  currentList: CurrentListUtil
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
        albumSave => {
          for {
            newAlbumId <- albumDAO.create(albumSave)
            newAlbum <- albumDAO.get(newAlbumId)
          } yield {
            cache.remove("coreData")
            Ok(Json.toJson(Album.fromDb(newAlbum)))
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
        albumSave => {
          for {
            _ <- albumDAO.update(albumId, albumSave)
            album <- albumDAO.get(albumId)
          } yield {
            cache.remove("coreData")
            currentList.refresh()
            cache.remove(s"album/${albumId.value}")
            Ok(Json.toJson(Album.fromDb(album)))
          }
        }
      )
    }
  }
}
