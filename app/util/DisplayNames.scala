package util

import javax.inject.{Inject, Singleton}
import model.db.dao.UserDAO
import play.api.Logger
import play.api.cache.AsyncCacheApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DisplayNames @Inject() (cache: AsyncCacheApi, userDAO: UserDAO) {
  val logger = Logger(getClass)

  def get(): Future[Map[String, String]] = {
    cache.get[Map[String, String]]("displayNames") flatMap {
      case Some(displayNames) =>
        Future.successful(displayNames)
      case None =>
        userDAO.getDisplayNames() flatMap { displayNames =>
          cache.set("displayNames", displayNames) map { _ =>
            displayNames
          }
        }
    }
  }
}
