package controllers

import javax.inject._

import model.ArtistId
import model.api.Artist
import model.db.dao.ArtistDAO
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ArtistController @Inject()(artistDAO: ArtistDAO) extends InjectedController {
  def get(artistId: ArtistId) = Action.async { implicit rs =>
    for {
      artist <- artistDAO.get(artistId)
    } yield Ok(Json.toJson(Artist.fromDb(artist)))
  }
}
