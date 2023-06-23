package util.coverartarchive

import com.typesafe.config.Config
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.nio.file.{Files, Path}

class CoverArtArchiveAPI @Inject()(ws: WSClient, config: Config) {
  private def sendRequest(musicbrainzId: String): WSRequest = {
    ws
      .url(s"https://coverartarchive.org/release-group/$musicbrainzId/front-250")
      .addHttpHeaders(
        "User-Agent" -> s"tijdloze.rocks crawler (https://tijdloze.rocks)",
        "Accept" -> "application/json"
      )
  }

  def searchAlbum(musicbrainzId: String): Future[Option[String]] = {
    sendRequest(musicbrainzId).get().map { response =>
      if (response.status == 200) {
        val path = Path.of(config.getString("tijdloze.covers.path"), s"$musicbrainzId.jpg")
        Files.write(path, response.bodyAsBytes.toArray)
        Some(musicbrainzId)
      } else {
        None
      }
    }
  }
}
