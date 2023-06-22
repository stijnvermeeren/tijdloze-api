package util.wikidata

import com.typesafe.config.Config
import model.api.SpotifyHit
import play.api.libs.json.{JsArray, JsString}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WikidataAPI @Inject()(ws: WSClient, config: Config) {
  def findBySpotifyId(spotifyId: String): Future[Seq[String]] = {
    def request() = {
      val query = s"""SELECT ?item
      WHERE {
        ?item wdt:P1902 '$spotifyId'
      }"""

      val request = ws
        .url("https://query.wikidata.org/sparql")
        .withQueryStringParameters(
          "query" -> query
        )
        .addHttpHeaders(
          "User-Agent" -> s"tijdloze.rocks crawler (https://tijdloze.rocks)",
            "Accept" -> "application/sparql-results+json"
        )

      request.get()
    }

    def handleResponse(response: WSResponse) = {
      val items = (response.json \ "results" \ "bindings").as[JsArray]
      items.value.toSeq flatMap { value =>
        (value \ "item" \ "value").toOption map { uri =>
          uri.as[JsString].value.split('/').last
        }
      }
    }

    request().map(handleResponse)
  }
}
