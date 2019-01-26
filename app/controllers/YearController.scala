package controllers

import javax.inject._
import model.db.dao.YearDAO
import play.api.cache.AsyncCacheApi
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class YearController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  yearDAO: YearDAO
) extends InjectedController {

  def post(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      yearDAO.save(year) map { _ =>
        cache.remove("coreData")
        cache.remove("currentList")
        Ok("")
      }
    }
  }
}
