package controllers

import model.{AlbumCrawlField, ArtistCrawlField}
import model.db.dao.{AlbumDAO, ArtistDAO}
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.wikidata.WikidataAPI
import util.wikipedia.WikipediaAPI

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WikidataController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  wikidataAPI: WikidataAPI,
  wikipediaAPI: WikipediaAPI,
  artistDAO: ArtistDAO,
  albumDAO: AlbumDAO,
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
    (Action andThen authenticateAdmin).async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        val artistsWithWikidataId = artists.flatMap { artist =>
          artist.wikidataId.map(wikidataId => (artist, wikidataId))
        }
        FutureUtil.traverseSequentially(artistsWithWikidataId) { case (artist, wikidataId) =>
          wikidataAPI.getDetailsById(wikidataId) flatMap { wikidataDetails =>
            println(wikidataDetails)
            val comment = s"Wikidata (${artist.wikidataId.getOrElse("")})"

            wikidataDetails.urlWikiEn.foreach(wikipediaAPI.reload)
            wikidataDetails.urlWikiNl.foreach(wikipediaAPI.reload)

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

  def crawlAlbumDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      albumDAO.getAll().flatMap{ albums =>
        val albumsWithWikidataId = albums.flatMap { album =>
          album.wikidataId.map(wikidataId => (album, wikidataId))
        }
        FutureUtil.traverseSequentially(albumsWithWikidataId) { case (album, wikidataId) =>
          wikidataAPI.getDetailsById(wikidataId) flatMap { wikidataDetails =>
            println(wikidataDetails)
            val comment = s"Wikidata (${album.wikidataId.getOrElse("")})"

            wikidataDetails.urlWikiEn.foreach(wikipediaAPI.reload)
            wikidataDetails.urlWikiNl.foreach(wikipediaAPI.reload)

            for {
              _ <- crawlHelper.processAlbum(
                album = album,
                field = AlbumCrawlField.UrlWikiEn,
                candidateValues = wikidataDetails.urlWikiEn,
                comment = comment,
                strategy = AutoIfUnique
              )
              _ <- crawlHelper.processAlbum(
                album = album,
                field = AlbumCrawlField.UrlWikiNl,
                candidateValues = wikidataDetails.urlWikiNl,
                comment = comment,
                strategy = AutoIfUnique
              )
              _ <- crawlHelper.processAlbum(
                album = album,
                field = AlbumCrawlField.UrlAllMusic,
                candidateValues = wikidataDetails.allMusicAlbumId.map(id => s"https://www.allmusic.com/album/$id"),
                comment = comment,
                strategy = AutoIfUnique
              )
              _ <- crawlHelper.processAlbum(
                album = album,
                field = AlbumCrawlField.MusicbrainzId,
                candidateValues = wikidataDetails.musicbrainzReleaseGroupId,
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
