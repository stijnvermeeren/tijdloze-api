package controllers

import model.ArtistCrawlField
import model.db.{Artist, Song}

import javax.inject._
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlArtistDAO, SongDAO}
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.spotify.{SpotifyAPI, SpotifyArtist}

import scala.concurrent.ExecutionContext

@Singleton
class SpotifyController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  spotifyAPI: SpotifyAPI,
  songDAO: SongDAO,
  artistDAO: ArtistDAO,
  crawlArtistDAO: CrawlArtistDAO,
  dataCache: DataCache
)(implicit ec: ExecutionContext) extends InjectedController {

  def crawlArtistsFromSongs() = {
    def setArtistSpotifyId(song: Song, artist: Artist, spotifyArtists: Seq[SpotifyArtist]) = {
      def saveAuto(spotifyId: String) = crawlArtistDAO.saveAuto(
        artist.id,
        field = ArtistCrawlField.SpotifyId,
        value = Some(spotifyId),
        comment = Some(s"Spotify song (${song.id.value} ${song.title})"),
        isAccepted = true
      ) flatMap { _ =>
        artistDAO.setSpotifyId(artist.id, Some(spotifyId))
      } map { _ =>
        dataCache.ArtistDataCache.reload(artist.id)
      }

      spotifyArtists.headOption.filter(_ => spotifyArtists.length == 1) match {
        case Some(uniqueSpotifyArtist) =>
          saveAuto(uniqueSpotifyArtist.id)
        case _ =>
          spotifyArtists.find(_.name == artist.name) match {
            case Some(matchingArtist) =>
              saveAuto(matchingArtist.id)
            case None =>
              FutureUtil.traverseSequentially(spotifyArtists) { spotifyArtist =>
                crawlArtistDAO.savePending(
                  artist.id,
                  field = ArtistCrawlField.SpotifyId,
                  value = Some(spotifyArtist.id),
                  comment = Some(s"Spotify song (${song.id} ${song.title})")
                )
              }
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
            spotifyAPI.getArtistsFromTrack(token, spotifyId) flatMap { spotifyArtists =>
              val mainArtist = for {
                artist <- artistDAO.get(song.artistId)
                _ <- setArtistSpotifyId(song, artist, spotifyArtists)
              } yield Thread.sleep(1000)

              song.secondArtistId match {
                case Some(secondArtistId) => mainArtist.flatMap{ _ =>
                  for {
                    artist <- artistDAO.get(secondArtistId)
                    _ <- setArtistSpotifyId(song, artist, spotifyArtists)
                  } yield Thread.sleep(1000)
                }
                case None => mainArtist
              }
            }
          }
        }
      } map { _ =>
        Ok
      }
    }
  }

  def find(query: String) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      spotifyAPI.getToken() flatMap { token =>
        spotifyAPI.findNewSong(token, query, limit=1) map { result =>
          Ok(Json.toJson(result))
        }
      }
    }
  }
}
