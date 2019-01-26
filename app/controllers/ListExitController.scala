package controllers

import javax.inject._
import model.SongId
import model.db.dao.ListExitDAO
import play.api.cache.AsyncCacheApi
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ListExitController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  listExitDAO: ListExitDAO
) extends InjectedController {

  def post(year: Int, songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.save(year, songId) map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }

  def delete(year: Int, songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.delete(year, songId) map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }

  def deleteAll(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      listExitDAO.deleteAll(year) map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }
}
