package controllers

import javax.inject._
import model.db.dao.{ListEntryDAO, YearDAO}
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class YearController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  yearDAO: YearDAO,
  listEntryDAO: ListEntryDAO,
  currentList: CurrentListUtil
) extends InjectedController {

  def post(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      yearDAO.save(year) map { _ =>
        cache.remove("coreData")
        currentList.updateCurrentYear(year)
        Ok("")
      }
    }
  }

  def delete(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      // only allow deleting a year if there are no entries in that year
      listEntryDAO.getByYear(year) flatMap { entries =>
        if (entries.isEmpty) {
          yearDAO.delete(year) flatMap { _ =>
            cache.remove("coreData")
            yearDAO.maxYear() map {
              case Some(maxYear) =>
                currentList.updateCurrentYear(maxYear)
                Ok("")
              case _ =>
                Ok("")
            }
          }
        } else {
          Future.successful(Forbidden)
        }
      }
    }
  }
}
