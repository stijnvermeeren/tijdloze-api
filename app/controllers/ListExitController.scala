package controllers

import javax.inject._
import model.SongId
import model.db.dao.ListExitDAO
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext

@Singleton
class ListExitController @Inject()(
  dataCache: DataCache,
  authenticateAdmin: AuthenticateAdmin,
  listExitDAO: ListExitDAO,
  currentList: CurrentListUtil
)(implicit ec: ExecutionContext) extends InjectedController {

  def post(year: Int, songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.save(year, songId) map { _ =>
        dataCache.CoreDataCache.reload()
        currentList.updateCurrentYear(year)
        Ok("")
      }
    }
  }

  def delete(year: Int, songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.delete(year, songId) map { _ =>
        dataCache.CoreDataCache.reload()
        currentList.updateCurrentYear(year)
        Ok("")
      }
    }
  }

  def deleteAll(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.deleteAll(year) map { _ =>
        dataCache.CoreDataCache.reload()
        currentList.updateCurrentYear(year)
        Ok("")
      }
    }
  }
}
