package util.wikidata

import com.typesafe.config.Config
import model.api.SpotifyHit
import play.api.libs.json.{JsArray, JsString}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WikidataAPI @Inject()(ws: WSClient, config: Config) {
  private def sparqlRequest(query: String): WSRequest = {
    ws
      .url("https://query.wikidata.org/sparql")
      .withQueryStringParameters(
        "query" -> query
      )
      .addHttpHeaders(
        "User-Agent" -> s"tijdloze.rocks crawler (https://tijdloze.rocks)",
        "Accept" -> "application/sparql-results+json"
      )
  }

  def findBySpotifyId(spotifyId: String): Future[Seq[String]] = {
    val query = s"""SELECT ?item
    WHERE {
      ?item wdt:P1902 '$spotifyId'
    }"""

    sparqlRequest(query).get().map { response =>
      val items = (response.json \ "results" \ "bindings").as[JsArray]
      items.value.toSeq flatMap { value =>
        (value \ "item" \ "value").toOption map { uri =>
          uri.as[JsString].value.split('/').last
        }
      }
    }
  }

  def getDetailsById(wikidataId: String): Future[WikidataDetails] = {
    val query =
      s"""
         |SELECT ?key ?value WHERE {
         |  BIND(<http://www.wikidata.org/entity/$wikidataId> AS ?id)
         |  {
         |    ?id wdt:P856 ?value
         |    BIND("url_official" AS ?key)
         |  }
         |  UNION {
         |    ?value schema:about ?id .
         |    ?value schema:isPartOf <https://en.wikipedia.org/> .
         |    BIND("url_wikien" AS ?key)
         |  }
         |  UNION {
         |    ?value schema:about ?id .
         |    ?value schema:isPartOf <https://nl.wikipedia.org/> .
         |    BIND("url_wikinl" AS ?key)
         |  }
         |  UNION {
         |    ?id wdt:P434 ?value
         |    BIND("musicbrainz_id" AS ?key)
         |  }
         |  UNION {
         |    ?id wdt:P495 ?country .
         |    ?country wdt:P297 ?value .
         |    BIND("country_id" AS ?key)
         |  }
         |  UNION {
         |    ?id wdt:P1728 ?value
         |    BIND("allmusic_id" AS ?key)
         |  }
         |}
         |""".stripMargin

    sparqlRequest(query).get().map { response =>
      val items = (response.json \ "results" \ "bindings").as[JsArray]

      def valuesForKey(key: String): Seq[String] = {
        items.value.toSeq.filter{entry =>
          (entry \ "key" \ "value").toOption.contains(JsString(key))
        } flatMap { entry =>
          (entry \ "value" \ "value").toOption.map(_.as[JsString].value)
        }
      }

      WikidataDetails(
        countryId = valuesForKey("country_id").map(_.toLowerCase),
        urlOfficial = valuesForKey("url_official"),
        urlWikiEn = valuesForKey("url_wikien"),
        urlWikiNl = valuesForKey("url_wikinl"),
        musicbrainzId = valuesForKey("musicbrainz_id"),
        allMusicId = valuesForKey("allmusic_id")
      )
    }
  }
}
