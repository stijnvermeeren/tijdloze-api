package util

import com.typesafe.config.Config
import javax.inject.Inject
import model.api.SpotifyHit
import play.api.libs.json.JsArray
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SpotifyAPI @Inject() (ws: WSClient, config: Config) {
  def getToken(): Future[String] = {
    val request = ws
      .url("https://accounts.spotify.com/api/token")
      .withAuth(
        username = config.getString("tijdloze.spotify.clientId"),
        password = config.getString("tijdloze.spotify.clientSecret"),
        WSAuthScheme.BASIC
      )

    request.post(Map("grant_type" -> "client_credentials")) map { response =>
      (response.json \ "access_token").as[String]
    }
  }

  def findNewSong(token: String, query: String, limit: Int): Future[Seq[SpotifyHit]] = {
    def request() = {
      val request = ws
        .url("https://api.spotify.com/v1/search")
        .addHttpHeaders("Authorization" -> s"Bearer $token")
        .addQueryStringParameters(
          "q" -> query,
          "type" -> "track",
          "market" -> "BE",
          "limit" -> s"$limit"
        )

      request.get()
    }

    def handleResponse(response: WSResponse) = {
      val items = (response.json \ "tracks" \ "items").as[JsArray]
      items.value.toSeq map { value =>
        val artists = (value \ "artists").as[JsArray]

        SpotifyHit(
          spotifyId = (value \ "id").as[String],
          title = (value \ "name").as[String],
          artist = (artists.value.head \ "name").as[String],
          album = (value \ "album" \ "name").as[String],
          year = Integer.parseInt((value \ "album" \ "release_date").as[String].take(4))
        )

      }
    }

    requestHandler(request, handleResponse)
  }

  def findSongId(token: String, artist: String, album: String, title: String): Future[Option[String]] = {
    def request() = {
      val request = ws
        .url("https://api.spotify.com/v1/search")
        .addHttpHeaders("Authorization" -> s"Bearer $token")
        .addQueryStringParameters(
          "q" -> s"$artist $title $album",
          "type" -> "track",
          "market" -> "BE",
          "limit" -> "1"
        )

      request.get()
    }

    def handleResponse(response: WSResponse) = {
      val items = (response.json \ "tracks" \ "items").as[JsArray]
      items.value.headOption flatMap { value =>
        val artists = (value \ "artists").as[JsArray]
        val spotifyArtist = (artists.value.head \ "name").as[String]
        val spotifyTitle = (value \ "name").as[String]
        val spotifyAlbum = (value \ "album" \ "name").as[String]


        val query = s"$artist - $title ($album)"
        val found = s"$spotifyArtist - $spotifyTitle ($spotifyAlbum)"
        println()
        if (query != found) {
          println(s"Query: $query")
          println(s"Found: $found")
        }

        if (artist == spotifyArtist) {
          Some((value \ "id").as[String])
        } else {
          None
        }

      }
    }

    requestHandler(request, handleResponse)
  }

  def requestHandler[T <: WSResponse, S](request: () => Future[T], process: T => S): Future[S] = {
    request() flatMap { response =>
      if (response.status == 429) {
        // pause for 1 second more than requested, just to be on the safe side
        val retryAfter = Integer.parseInt(response.header("Retry-After").get) + 1
        println(s"Spotify API rate limit reached, waiting for $retryAfter seconds.")
        // TODO: don't block the thread
        Thread.sleep(1000 * retryAfter)

        request() map process
      } else {
        Future.successful(process(response))
      }
    }
  }
}
