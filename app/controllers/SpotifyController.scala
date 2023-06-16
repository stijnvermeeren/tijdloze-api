package controllers

import model.db.{Artist, Song}

import javax.inject._
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlArtistDAO, SongDAO}
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.spotify.{SpotifyAPI, SpotifyArtist}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SpotifyController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  spotifyAPI: SpotifyAPI,
  songDAO: SongDAO,
  artistDAO: ArtistDAO,
  albumDAO: AlbumDAO,
  crawlArtistDAO: CrawlArtistDAO
) extends InjectedController {

  def crawlArtistsFromSongs() = {
    def setArtistSpotifyId(song: Song, artist: Artist, spotifyArtists: Seq[SpotifyArtist]) = {
      spotifyArtists.headOption.filter(_ => spotifyArtists.length == 1) match {
        case Some(uniqueSpotifyArtist) =>
          crawlArtistDAO.saveAuto(
            artist.id,
            field = "spotifyId",
            value = Some(uniqueSpotifyArtist.id),
            comment = Some(s"Spotify song (${song.id} ${song.title})"),
            isAccepted = true
          ) flatMap { _ =>
            artistDAO.setSpotifyId(artist.id, uniqueSpotifyArtist.id)
          }
        case _ =>
          FutureUtil.traverseSequentially(spotifyArtists) { spotifyArtist =>
            crawlArtistDAO.savePending(
              artist.id,
              field = "spotifyId",
              value = Some(spotifyArtist.id),
              comment = Some(s"Spotify song (${song.id} ${song.title})")
            )
          }
      }
    }

    Action.async { implicit request =>
      spotifyAPI.getToken() flatMap { token =>
        songDAO.getAll().flatMap{ songs =>
          val songsWithSpotifyId = songs.flatMap { song =>
            song.spotifyId.map(spotifyId => (song, spotifyId))
          }
          FutureUtil.traverseSequentially(songsWithSpotifyId) { case (song, spotifyId) =>
            for {
              artist <- artistDAO.get(song.artistId)
              spotifyArtists <- spotifyAPI.getArtistsFromTrack(token, spotifyId)
              _ <- setArtistSpotifyId(song, artist, spotifyArtists)
            } yield Thread.sleep(1000)
          }
        }
      } map { _ =>
        Ok
      }
    }
  }

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
