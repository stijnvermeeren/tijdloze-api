package controllers

import javax.inject._
import model.api.ListEntrySave
import model.db.dao.ListEntryDAO
import play.api.cache.AsyncCacheApi
import play.api.libs.json.JsError
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ListEntryController @Inject()(cache: AsyncCacheApi, authenticate: Authenticate, listEntryDAO: ListEntryDAO) extends InjectedController {
  def post(year: Int, position: Int) = {
    (Action andThen authenticate).async(parse.json) { implicit request =>
      val data = request.body.validate[ListEntrySave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        listEntrySave => {
          listEntryDAO.save(year = year, position = position, songId = listEntrySave.songId) map { _ =>
            cache.remove("coreData")
            Ok("")
          }
        }
      )
    }
  }

  def delete(year: Int, position: Int) = {
    (Action andThen authenticate).async { implicit request =>
      listEntryDAO.delete(year = year, position = position) map { _ =>
        cache.remove("coreData")
        Ok("")
      }
    }
  }
}
