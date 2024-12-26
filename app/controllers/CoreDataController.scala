package controllers

import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class CoreDataController @Inject()(
  dataCache: DataCache
)(implicit ec: ExecutionContext) extends InjectedController {

  def get() = {
    Action.async { implicit rs =>
      dataCache.CoreDataCache.load()
    }
  }
}
