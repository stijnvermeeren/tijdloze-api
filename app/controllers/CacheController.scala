package controllers

import javax.inject._
import play.api.cache.AsyncCacheApi
import play.api.mvc._

@Singleton
class CacheController @Inject()(authenticateAdmin: AuthenticateAdmin, cache: AsyncCacheApi) extends InjectedController {
  def invalidate() = {
    (Action andThen authenticateAdmin) { implicit rs =>
      cache.removeAll()
      Ok("")
    }
  }
}
