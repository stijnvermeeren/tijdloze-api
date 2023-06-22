package controllers

import model.CrawlField
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlArtistDAO, SongDAO}
import model.db.{Artist, Song}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.wikidata.WikidataAPI

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WikidataController @Inject()(
  wikidataAPI: WikidataAPI,
  songDAO: SongDAO,
  artistDAO: ArtistDAO,
  crawlArtistDAO: CrawlArtistDAO,
  crawlHelper: CrawlHelper,
  cache: AsyncCacheApi
) extends InjectedController {

  def crawlArtistsFromSpotify() = {
    Action.async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        val artistsWithSpotifyId = artists.flatMap { artist =>
          artist.spotifyId.map(spotifyId => (artist, spotifyId))
        }
        FutureUtil.traverseSequentially(artistsWithSpotifyId) { case (artist, spotifyId) =>
          wikidataAPI.findBySpotifyId(spotifyId) flatMap { wikidataIds =>
            for {
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.WikidataId,
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
    Action.async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        val artistsWithWikidataId = artists.flatMap { artist =>
          artist.wikidataId.map(wikidataId => (artist, wikidataId))
        }
        FutureUtil.traverseSequentially(artistsWithWikidataId) { case (artist, spotifyId) =>
          wikidataAPI.getDetailsById(spotifyId) flatMap { wikidataDetails =>
            val comment = s"Wikidata (${artist.wikidataId.getOrElse("")})"
            for {
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.CountryId,
                candidateValues = wikidataDetails.countryId,
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.UrlWikiEn,
                candidateValues = wikidataDetails.urlWikiEn,
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.UrlWikiNl,
                candidateValues = wikidataDetails.urlWikiNl,
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.UrlOfficial,
                candidateValues = wikidataDetails.urlOfficial,
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.UrlAllMusic,
                candidateValues = wikidataDetails.allMusicId.map(id => s"https://www.allmusic.com/artist/$id"),
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.process(
                artist = artist,
                field = CrawlField.MusicbrainzId,
                candidateValues = wikidataDetails.musicbrainzId,
                comment = comment,
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
}
