package controllers

import javax.inject._

import model.AlbumId
import model.api.Album
import model.db.dao.AlbumDAO
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AlbumController @Inject()(albumDAO: AlbumDAO) extends InjectedController {
  def get(albumId: AlbumId) = Action.async { implicit rs =>
    for {
      album <- albumDAO.get(albumId)
    } yield Ok(Json.toJson(Album.fromDb(album)))
  }
}
