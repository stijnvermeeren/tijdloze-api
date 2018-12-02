package controllers

import javax.inject._

import model.SongId
import model.api.Song
import model.db.dao.SongDAO
import play.api.cache.Cached
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SongController @Inject()(cached: Cached, songDAO: SongDAO) extends InjectedController {
  def get(songId: SongId) = {
    // cached(s"song/${songId.value}") {
      Action.async { implicit rs =>
        for {
          song <- songDAO.get(songId)
        } yield Ok(Json.toJson(Song.fromDb(song)))
      }
    // }
  }
}
