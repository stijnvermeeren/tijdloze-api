package controllers

import model.ArtistCrawlField
import model.db.dao.{AlbumDAO, ArtistDAO, SongDAO}
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, CrawlHelper}
import util.wikidata.{WikidataAPI, WikidataCrawler}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WikidataController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  wikidataAPI: WikidataAPI,
  artistDAO: ArtistDAO,
  albumDAO: AlbumDAO,
  songDAO: SongDAO,
  crawlHelper: CrawlHelper,
  wikidataCrawler: WikidataCrawler
)(implicit ec: ExecutionContext) extends InjectedController {

  def crawlArtistsFromSpotify() = {
    Action.async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        val artistsWithSpotifyId = artists.flatMap { artist =>
          artist.spotifyId.map(spotifyId => (artist, spotifyId))
        }
        FutureUtil.traverseSequentially(artistsWithSpotifyId) { case (artist, spotifyId) =>
          wikidataAPI.findBySpotifyId(spotifyId) flatMap { wikidataIds =>
            for {
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.WikidataId,
                candidateValues = wikidataIds,
                comment = s"Spotify id (${artist.spotifyId.getOrElse("")})",
                strategy = AutoIfUnique
              )
            } yield Thread.sleep(1000)
          }
        }
      } map { _ =>
        Ok
      }
    }
  }

  def crawlArtistDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        FutureUtil.traverseSequentially(artists)(wikidataCrawler.crawlArtistDetails)
      } map { _ =>
        Ok
      }
    }
  }

  def crawlAlbumDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      albumDAO.getAll().flatMap{ albums =>
        FutureUtil.traverseSequentially(albums)(wikidataCrawler.crawlAlbumDetails)
      } map { _ =>
        Ok
      }
    }
  }
  
  def crawlSongDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      songDAO.getAll().flatMap{ songs =>
        FutureUtil.traverseSequentially(songs)(wikidataCrawler.crawlSongDetails)
      } map { _ =>
        Ok
      }
    }
  }
}
