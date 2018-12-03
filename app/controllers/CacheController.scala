package controllers

import javax.inject._
import play.api.cache.AsyncCacheApi
import play.api.mvc._

@Singleton
class CacheController @Inject()(cache: AsyncCacheApi) extends InjectedController {
  def invalidate() = {
    Action { implicit rs =>
      cache.removeAll()
      Ok("")
    }
  }
}
