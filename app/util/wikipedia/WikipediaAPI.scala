package util.wikipedia

import model.db.dao.WikipediaContentDAO
import play.api.libs.json.{JsObject, JsString}
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WikipediaAPI @Inject()(
  ws: WSClient,
  wikipediaContentDAO: WikipediaContentDAO
) {
  private def request(language: String, title: String): WSRequest = {
    ws
      .url(s"https://${language}.wikipedia.org/w/api.php")
      .withQueryStringParameters(
        "format" -> "json",
        "action" -> "query",
        "prop" -> "extracts",
        "titles" -> title,
        "exintro" -> ""
      )
      .addHttpHeaders(
        "User-Agent" -> s"tijdloze.rocks crawler (https://tijdloze.rocks)"
      )
  }

  def extractByUrl(url: String): Future[Option[String]] = {
    val pattern = """https://([a-z]+)\.wikipedia\.org/wiki/(.+)""".r
    url match {
      case pattern(language, title) =>
        request(language, title).get().map { response =>
          val items = (response.json \ "query" \ "pages").as[JsObject]
          items.values.headOption.map(_.as[JsObject]) flatMap { value =>
            (value \ "extract").toOption map { extract =>
              extract.as[JsString].value
            }
          }
        }
      case _ =>
        Future.successful(None)
    }
  }

  def reload(url: String): Future[Unit] = {
    extractByUrl(url) flatMap {
      case Some(content) =>
        wikipediaContentDAO.insertOrUpdate(url, content)
      case None =>
        wikipediaContentDAO.delete(url)
    } map (_ => ())
  }
}
