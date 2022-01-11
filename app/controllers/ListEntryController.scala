package controllers

import javax.inject._
import model.api.ListEntrySave
import model.db.dao.ListEntryDAO
import play.api.cache.AsyncCacheApi
import play.api.libs.json.JsError
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ListEntryController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  listEntryDAO: ListEntryDAO,
  currentList: CurrentListUtil
) extends InjectedController {
  def post(year: Int, position: Int) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[ListEntrySave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        listEntrySave => {
          listEntryDAO.save(year = year, position = position, songId = listEntrySave.songId) map { _ =>
            cache.remove("coreData")
            currentList.updateEntry(year, position, songId = Some(listEntrySave.songId))
            Ok("")
          }
        }
      )
    }
  }

  def delete(year: Int, position: Int) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      listEntryDAO.delete(year = year, position = position) map { _ =>
        cache.remove("coreData")
        currentList.updateEntry(year, position, songId = None)
        Ok("")
      }
    }
  }
}
