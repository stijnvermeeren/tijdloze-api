package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class CacheController @Inject()(
    authenticateAdmin: AuthenticateAdmin,
    dataCache: DataCache
) extends InjectedController {
  def invalidate() = {
    (Action andThen authenticateAdmin) { implicit rs =>
      dataCache.CoreDataCache.reload()
      dataCache.ArtistDataCache.removeAll()
      dataCache.AlbumDataCache.removeAll()
      dataCache.SongDataCache.removeAll()
      dataCache.TextCache.removeAll()
      Ok("")
    }
  }
}
