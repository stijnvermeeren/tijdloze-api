package controllers

import model.db.dao.MBDataDAO
import play.api.mvc._

import javax.inject._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MBDataController @Inject()(
  mbDataDAO: MBDataDAO
) extends InjectedController {
  def search(artistQuery: String, titleQuery: String) = {
    Action.async { implicit request =>
      mbDataDAO.get(artistQuery, titleQuery) map {
        Ok(_)
      }
    }
  }
}
