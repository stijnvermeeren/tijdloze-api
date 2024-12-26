package controllers

import model.api.MBDatasetResponse
import model.db.dao.MBDataDAO
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class MBDataController @Inject()(
  mbDataDAO: MBDataDAO
)(implicit ec: ExecutionContext) extends InjectedController {
  def search(artistQuery: String, titleQuery: String) = {
    Action.async { implicit request =>
      mbDataDAO.searchArtistTitle(artistQuery, titleQuery) map { hit =>
        Ok(Json.toJson(MBDatasetResponse(hit)))
      }
    }
  }

  def searchQuery(query: String) = {
    Action.async { implicit request =>
      mbDataDAO.searchQuery(query) map { hit =>
        Ok(Json.toJson(MBDatasetResponse(hit)))
      }
    }
  }
}
