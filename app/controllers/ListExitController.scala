package controllers

import javax.inject._
import model.SongId
import model.db.dao.ListExitDAO
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ListExitController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  listExitDAO: ListExitDAO,
  currentList: CurrentListUtil
) extends InjectedController {

  def post(year: Int, songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.save(year, songId) map { _ =>
        cache.remove("coreData")
        currentList.refresh(year)
        Ok("")
      }
    }
  }

  def delete(year: Int, songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.delete(year, songId) map { _ =>
        cache.remove("coreData")
        currentList.refresh(year)
        Ok("")
      }
    }
  }

  def deleteAll(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.deleteAll(year) map { _ =>
        cache.remove("coreData")
        currentList.refresh(year)
        Ok("")
      }
    }
  }
}
