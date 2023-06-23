package util.musicbrainz

import com.typesafe.config.Config
import model.db.{Album, Artist}
import play.api.libs.json.{JsArray, JsString}
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MusicbrainzAPI @Inject()(ws: WSClient, config: Config) {
  private def sendRequest(endpoint: String, query: String): WSRequest = {
    ws
      .url(s"http://musicbrainz.org/ws/2/$endpoint/")
      .withQueryStringParameters(
        "query" -> query
      )
      .addHttpHeaders(
        "User-Agent" -> s"tijdloze.rocks crawler (https://tijdloze.rocks)",
        "Accept" -> "application/json"
      )
  }

  def searchAlbum(album: Album, artist: Artist): Future[Seq[String]] = {
    val artistQuery = artist.musicbrainzId match {
      case Some(musicbrainzId) => s"""arid:"$musicbrainzId""""
      case None => s"""artist:"${artist.fullName}""""
    }

    val singleSuffix = "(single)"
    val searchTitle = if (album.title.endsWith(singleSuffix)) {
      album.title.dropRight(singleSuffix.length).trim
    } else {
      album.title
    }
    val titleQuery = s"""release:"$searchTitle""""

    val yearQuery = s"""firstreleasedate:${album.releaseYear}"""

    val query = s"$artistQuery AND $titleQuery AND $yearQuery"
    sendRequest("release-group", query).get().map { response =>
      val items = (response.json \ "release-groups").as[JsArray]
      items.value.toSeq flatMap { value =>
        (value \ "id").toOption map { id =>
          id.as[JsString].value
        }
      }
    }
  }
}
