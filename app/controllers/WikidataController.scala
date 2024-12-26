package controllers

import model.ArtistCrawlField
import model.db.dao.ArtistDAO
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.wikidata.WikidataAPI

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WikidataController @Inject()(
  wikidataAPI: WikidataAPI,
  artistDAO: ArtistDAO,
  crawlHelper: CrawlHelper
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
    Action.async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        val artistsWithWikidataId = artists.flatMap { artist =>
          artist.wikidataId.map(wikidataId => (artist, wikidataId))
        }
        FutureUtil.traverseSequentially(artistsWithWikidataId) { case (artist, spotifyId) =>
          wikidataAPI.getDetailsById(spotifyId) flatMap { wikidataDetails =>
            val comment = s"Wikidata (${artist.wikidataId.getOrElse("")})"
            for {
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.CountryId,
                candidateValues = wikidataDetails.countryId,
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.UrlWikiEn,
                candidateValues = wikidataDetails.urlWikiEn,
                comment = comment,
                strategy = AutoIfUnique
              )
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.UrlWikiNl,
                candidateValues = wikidataDetails.urlWikiNl,
                comment = comment,
                strategy = AutoIfUnique
              )
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.UrlOfficial,
                candidateValues = wikidataDetails.urlOfficial,
                comment = comment,
                strategy = AutoOnlyForExistingValue
              )
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.UrlAllMusic,
                candidateValues = wikidataDetails.allMusicId.map(id => s"https://www.allmusic.com/artist/$id"),
                comment = comment,
                strategy = AutoIfUnique
              )
              _ <- crawlHelper.processArtist(
                artist = artist,
                field = ArtistCrawlField.MusicbrainzId,
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
