
package util.wikidata

import model.db.{Album, Artist, Song}
import model.{AlbumCrawlField, ArtistCrawlField, SongCrawlField}
import play.api.mvc._
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.wikipedia.WikipediaAPI

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WikidataCrawler @Inject()(
  wikidataAPI: WikidataAPI,
  wikipediaAPI: WikipediaAPI,
  crawlHelper: CrawlHelper
)(implicit ec: ExecutionContext) extends InjectedController {

  def crawlArtistDetails(artist: Artist) = {
    artist.wikidataId match {
      case Some(wikidataId) =>
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
      case None =>
        Future.successful(())
    }
  }

  def crawlAlbumDetails(album: Album) = {
    album.wikidataId match {
      case Some(wikidataId) =>
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
      case None =>
        Future.successful(None)
    }
  }
  
  def crawlSongDetails(song: Song) = {
    song.wikidataId match {
      case Some(wikidataId) =>
        wikidataAPI.getDetailsById(wikidataId) flatMap { wikidataDetails =>
          println(wikidataDetails)
          val comment = s"Wikidata (${song.wikidataId.getOrElse("")})"

          wikidataDetails.urlWikiEn.foreach(wikipediaAPI.reload)
          wikidataDetails.urlWikiNl.foreach(wikipediaAPI.reload)

          for {
            _ <- crawlHelper.processSong(
              song = song,
              field = SongCrawlField.UrlWikiEn,
              candidateValues = wikidataDetails.urlWikiEn,
              comment = comment,
              strategy = AutoIfUnique
            )
            _ <- crawlHelper.processSong(
              song = song,
              field = SongCrawlField.UrlWikiNl,
              candidateValues = wikidataDetails.urlWikiNl,
              comment = comment,
              strategy = AutoIfUnique
            )
          } yield Thread.sleep(1000)
        }
      case None =>
        Future.successful(None)
    }
  }
}
