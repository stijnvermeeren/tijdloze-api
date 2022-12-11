package controllers

import javax.inject._
import model.db.dao.{AlbumDAO, ArtistDAO, SongDAO}
import play.api.libs.json.Json
import play.api.mvc._
import util.SpotifyAPI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SpotifyController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  spotifyAPI: SpotifyAPI,
  songDAO: SongDAO,
  artistDAO: ArtistDAO,
  albumDAO: AlbumDAO
) extends InjectedController {

  def find() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      request.getQueryString("query") match {
        case Some(query) =>
          val limit = Integer.parseInt(request.getQueryString("limit").getOrElse("5"))

          spotifyAPI.getToken() flatMap { token =>
            spotifyAPI.findNewSong(token, query, limit) map { result =>
              Ok(Json.toJson(result))
            }
          }
        case None =>
          Future.successful(BadRequest("No query specified."))
      }
    }
  }
}
