package controllers

import javax.inject._
import model.SongId
import model.api.{Song, SongSave}
import model.db.dao.SongDAO
import play.api.cache.{AsyncCacheApi, Cached}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SongController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  cache: AsyncCacheApi,
  cached: Cached,
  songDAO: SongDAO,
  currentList: CurrentListUtil
) extends InjectedController {

  def get(songId: SongId) = {
    cached(s"song/${songId.value}") {
      Action.async { implicit rs =>
        for {
          song <- songDAO.get(songId)
        } yield Ok(Json.toJson(Song.fromDb(song)))
      }
    }
  }

  def post() = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[SongSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        songSave => {
          for {
            newSongId <- songDAO.create(songSave)
            newSong <- songDAO.get(newSongId)
          } yield {
            cache.remove("coreData")
            currentList.updateSong(Song.fromDb(newSong))
            Ok(Json.toJson(Song.fromDb(newSong)))
          }
        }
      )
    }
  }

  def put(songId: SongId) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[SongSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        songSave => {
          for {
            _ <- songDAO.update(songId, songSave)
            song <- songDAO.get(songId)
          } yield {
            cache.remove("coreData")
            currentList.updateSong(Song.fromDb(song))
            cache.remove(s"song/${songId.value}")
            Ok(Json.toJson(Song.fromDb(song)))
          }
        }
      )
    }
  }

  def delete(songId: SongId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      for {
        _ <- songDAO.delete(songId)
      } yield {
        cache.remove("coreData")
        currentList.deleteSong(songId)
        cache.remove(s"song/${songId.value}")
        Ok("")
      }
    }
  }
}
