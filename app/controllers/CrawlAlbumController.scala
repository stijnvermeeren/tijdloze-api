package controllers

import model.CrawlAlbumId
import model.db.dao.{AlbumDAO, CrawlAlbumDAO}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CrawlAlbumController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  crawlAlbumDAO: CrawlAlbumDAO,
  albumDAO: AlbumDAO,
  dataCache: DataCache
)(implicit ec: ExecutionContext) extends InjectedController {
  def getFirstPending() = {
    (Action andThen authenticateAdmin).async { implicit rs =>
      for {
        crawls <- crawlAlbumDAO.getFirstPending()
      } yield Ok(Json.toJson(crawls))
    }
  }

  def accept(crawlAlbumId: CrawlAlbumId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      crawlAlbumDAO.findById(crawlAlbumId).flatMap{
        case Some(crawl) =>
          for {
            _ <- crawl.field.save(albumDAO)(crawl.albumId, crawl.value)
            _ <- crawlAlbumDAO.accept(crawlAlbumId)
          } yield {
            dataCache.AlbumDataCache.reload(crawl.albumId)
            Ok
          }
        case None => Future.successful(Ok)
      }
    }
  }

  def reject(crawlAlbumId: CrawlAlbumId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      crawlAlbumDAO.reject(crawlAlbumId).map(_ => Ok)
    }
  }
}
