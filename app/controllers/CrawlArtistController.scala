package controllers

import model.CrawlArtistId
import model.db.dao.{ArtistDAO, CrawlArtistDAO}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CrawlArtistController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  crawlArtistDAO: CrawlArtistDAO,
  artistDAO: ArtistDAO,
  cache: AsyncCacheApi
) extends InjectedController {
  def getFirstPending() = {
    (Action andThen authenticateAdmin).async { implicit rs =>
      for {
        crawls <- crawlArtistDAO.getFirstPending()
      } yield Ok(Json.toJson(crawls))
    }
  }

  def accept(crawlArtistId: CrawlArtistId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      crawlArtistDAO.findById(crawlArtistId).flatMap{
        case Some(crawl) =>
          for {
            _ <- crawl.field.save(artistDAO)(crawl.artistId, crawl.value)
            _ <- crawlArtistDAO.accept(crawlArtistId)
          } yield {
            cache.remove(s"artist/${crawl.artistId.value}")
            Ok
          }
        case None => Future.successful(Ok)
      }
    }
  }

  def reject(crawlArtistId: CrawlArtistId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      crawlArtistDAO.reject(crawlArtistId).map(_ => Ok)
    }
  }
}
