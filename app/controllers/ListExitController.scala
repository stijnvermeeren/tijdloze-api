package controllers

import javax.inject._
import model.SongId
import model.db.dao.SongDAO
import play.api.cache.AsyncCacheApi
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ListExitController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  songDAO: SongDAO
) extends InjectedController {

  def post(songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      songDAO.markExit(songId) map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }

  def delete(songId: SongId) = {
    (Action andThen authenticateAdmin).async { request =>
      songDAO.unmarkExit(songId) map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }

  def deleteAll() = {
    (Action andThen authenticateAdmin).async { request =>
      songDAO.unmarkAllExit() map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }
}
