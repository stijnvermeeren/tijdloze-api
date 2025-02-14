package controllers

import model.api.WikipediaContent
import model.db.dao.WikipediaContentDAO
import play.api.libs.json.Json
import play.api.mvc._
import util.wikipedia.WikipediaAPI

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class WikipediaController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  wikipediaAPI: WikipediaAPI,
  wikipediaContentDAO: WikipediaContentDAO
)(implicit ec: ExecutionContext) extends InjectedController {

  def find(url: String) = {
    Action.async { implicit request =>
      wikipediaContentDAO.find(url) map {
        case Some(dbWikipediaContent) =>
          Ok(Json.toJson(WikipediaContent.fromDb(dbWikipediaContent)))
        case None =>
          NotFound
      }
    }
  }

  def reload(url: String) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      wikipediaAPI.reload(url) map (_ => Ok)
    }
  }
}
