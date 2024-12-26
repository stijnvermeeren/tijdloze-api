package controllers

import javax.inject._
import model.api.ListEntrySave
import model.db.dao.ListEntryDAO
import play.api.libs.json.JsError
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListEntryController @Inject()(
  dataCache: DataCache,
  authenticateAdmin: AuthenticateAdmin,
  listEntryDAO: ListEntryDAO,
  currentList: CurrentListUtil
)(implicit ec: ExecutionContext) extends InjectedController {
  def post(year: Int, position: Int) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[ListEntrySave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        listEntrySave => {
          listEntryDAO.save(year = year, position = position, songId = listEntrySave.songId) map { _ =>
            dataCache.CoreDataCache.reload()
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
        dataCache.CoreDataCache.reload()
        currentList.updateEntry(year, position, songId = None)
        Ok("")
      }
    }
  }
}
