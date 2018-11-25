package util

import com.typesafe.config.Config
import javax.inject.Inject
import play.api.libs.json.JsArray
import play.api.libs.ws.{WSAuthScheme, WSClient}

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

  def findSongId(token: String, artist: String, album: String, title: String): Future[Option[String]] = {
    val request = ws
      .url("https://api.spotify.com/v1/search")
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .addQueryStringParameters(
        "q" -> s"$artist $title $album",
        "type" -> "track",
        "market" -> "BE",
        "limit" -> "1"
      )

    request.get() map { response =>
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
          println(s"Query: ")
          println(s"Found: ")
        }

        if (artist == spotifyArtist) {
          Some((value \ "id").as[String])
        } else {
          None
        }

      }
    }
  }
}
