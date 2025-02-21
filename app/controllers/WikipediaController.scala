package controllers

import model.api.WikipediaContent
import model.db.dao.{AlbumDAO, ArtistDAO, SongDAO, WikipediaContentDAO}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.wikipedia.WikipediaAPI

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WikipediaController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  wikipediaAPI: WikipediaAPI,
  wikipediaContentDAO: WikipediaContentDAO,
  songDAO: SongDAO,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO
)(implicit ec: ExecutionContext) extends InjectedController with Logging {
  def find(url: String) = {
    Action.async { implicit request =>
      wikipediaContentDAO.find(url) map {
        case Some(dbWikipediaContent) =>
          Ok(Json.toJson(WikipediaContent.fromDb(dbWikipediaContent)))
        case None =>
          NotFound
      }
    }
  }

  def reload(url: String) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      wikipediaAPI.reload(url) map (_ => Ok)
    }
  }

  def crawl() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      for {
        _ <- albumDAO.getAll() flatMap { albums =>
          FutureUtil.traverseSequentially(albums) { album =>
            for {
              _ <- album.urlWikiEn match {
                case Some(url) =>
                  logger.info(s"Crawling ${url}")
                  wikipediaAPI.reload(url)
                case None => Future.successful(())
              }
              _ <- album.urlWikiNl match {
                case Some(url) =>
                  logger.info(s"Crawling ${url}")
                  wikipediaAPI.reload(url)
                case None => Future.successful(())
              }
            } yield ()
          }
        }
        _ <- songDAO.getAll() flatMap { songs =>
          FutureUtil.traverseSequentially(songs) { song =>
            for {
              _ <- song.urlWikiEn match {
                case Some(url) =>
                  logger.info(s"Crawling ${url}")
                  wikipediaAPI.reload(url)
                case None => Future.successful(())
              }
              _ <- song.urlWikiNl match {
                case Some(url) =>
                  logger.info(s"Crawling ${url}")
                  wikipediaAPI.reload(url)
                case None => Future.successful(())
              }
            } yield ()
          }
        }
        _ <- artistDAO.getAll() flatMap { artists =>
          FutureUtil.traverseSequentially(artists) { artist =>
            for {
              _ <- artist.urlWikiEn match {
                case Some(url) =>
                  logger.info(s"Crawling ${url}")
                  wikipediaAPI.reload(url)
                case None => Future.successful(())
              }
              _ <- artist.urlWikiNl match {
                case Some(url) =>
                  logger.info(s"Crawling ${url}")
                  wikipediaAPI.reload(url)
                case None => Future.successful(())
              }
            } yield ()
          }
        }
      } yield Ok
    }
  }
}
