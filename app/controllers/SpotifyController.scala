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
  authenticate: Authenticate,
  spotifyAPI: SpotifyAPI,
  songDAO: SongDAO,
  artistDAO: ArtistDAO,
  albumDAO: AlbumDAO
) extends InjectedController {

  def find() = {
    (Action andThen authenticate).async { implicit request =>
      request.getQueryString("query") match {
        case Some(query) =>
          spotifyAPI.getToken() flatMap { token =>
            spotifyAPI.findNewSong(token, query) map { result =>
              Ok(Json.toJson(result))
            }
          }
        case None =>
          Future.successful(BadRequest("No query specified."))
      }
    }
  }

  def load() = {
    (Action andThen authenticate).async { implicit request =>
      spotifyAPI.getToken() flatMap { token =>
        songDAO.getAll() flatMap { allSongs =>
          artistDAO.getAll() flatMap { allArtists =>
            val artistsById = allArtists.groupBy(_.id).mapValues(_.head)
            albumDAO.getAll() flatMap { allAlbums =>
              val albumsById = allAlbums.groupBy(_.id).mapValues(_.head)

              allSongs.foldLeft(Future.successful(())) { case (result, song) =>
                result flatMap { _ =>
                  val title = song.title
                  val artist = artistsById(song.artistId)
                  val artistName = s"${artist.firstName} ${artist.name}".trim
                  val album = albumsById(song.albumId).title
                  // s"${coreSong.title} ${coreArtist.firstName} ${coreArtist.name}"
                  spotifyAPI.findSongId(token = token, artist = artistName, album = album, title = title) flatMap {
                    case Some(spotifyId) =>
                      println(s"Processed: $artistName - $title")
                      songDAO.setSpotifyId(song.id, Some(spotifyId)) map (_ => ())
                    case None =>
                      println(s"!!! Not found: $artistName - $title")
                      Future.successful(())
                  }
                }
              }
            }
          }
        }
      } map { _ =>
        Ok("ok")
      }
    }
  }
}
