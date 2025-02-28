package controllers

import model.{CrawlArtistId, CrawlSongId}
import model.db.dao.{CrawlSongDAO, SongDAO}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CrawlSongController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  crawlSongDAO: CrawlSongDAO,
  songDAO: SongDAO,
  dataCache: DataCache
)(implicit ec: ExecutionContext) extends InjectedController {
  def getFirstPending() = {
    (Action andThen authenticateAdmin).async { implicit rs =>
      for {
        crawls <- crawlSongDAO.getFirstPending()
      } yield Ok(Json.toJson(crawls))
    }
  }

  def accept(crawlSongId: CrawlSongId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      crawlSongDAO.findById(crawlSongId).flatMap{
        case Some(crawl) =>
          for {
            _ <- crawl.field.save(songDAO)(crawl.songId, crawl.value)
            _ <- crawlSongDAO.accept(crawlSongId)
          } yield {
            dataCache.SongDataCache.reload(crawl.songId)
            Ok
          }
        case None => Future.successful(Ok)
      }
    }
  }

  def reject(crawlSongId: CrawlSongId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      crawlSongDAO.reject(crawlSongId).map(_ => Ok)
    }
  }
}
