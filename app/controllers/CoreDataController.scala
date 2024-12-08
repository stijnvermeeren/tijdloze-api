package controllers

import javax.inject._
import model.api._
import model.db.dao._
import play.api.cache.Cached
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CoreDataController @Inject()(
  dataCache: DataCache
) extends InjectedController {

  def get() = {
    Action.async { implicit rs =>
      dataCache.CoreDataCache.load()
    }
  }
}
