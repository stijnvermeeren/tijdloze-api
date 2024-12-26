package controllers

import javax.inject._
import model.db.dao.{ListEntryDAO, YearDAO}
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class YearController @Inject()(
  dataCache: DataCache,
  authenticateAdmin: AuthenticateAdmin,
  yearDAO: YearDAO,
  listEntryDAO: ListEntryDAO,
  currentList: CurrentListUtil
)(implicit ec: ExecutionContext) extends InjectedController {

  def post(year: Int) = {
    (Action andThen authenticateAdmin).async { request =>
      yearDAO.save(year) map { _ =>
        dataCache.CoreDataCache.reload()
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
            dataCache.CoreDataCache.reload()
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
