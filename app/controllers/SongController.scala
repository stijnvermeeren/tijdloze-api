package controllers

import javax.inject._
import model.SongId
import model.api.{Song, SongSave}
import model.db.dao.SongDAO
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SongController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  dataCache: DataCache,
  songDAO: SongDAO,
  currentList: CurrentListUtil
)(implicit ec: ExecutionContext) extends InjectedController {

  def get(songId: SongId) = {
    Action.async { implicit rs =>
      dataCache.SongDataCache.load(songId)
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
            _ <- dataCache.reloadSong(newSongId)
          } yield {
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
            _ <- dataCache.reloadSong(songId)
          } yield {
            currentList.updateSong(Song.fromDb(song))
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
        _ <- dataCache.CoreDataCache.reload()
      } yield {
        dataCache.SongDataCache.remove(songId)
        currentList.deleteSong(songId)
        Ok("")
      }
    }
  }
}
