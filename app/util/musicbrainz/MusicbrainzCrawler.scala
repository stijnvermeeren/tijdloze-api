package util.musicbrainz

import model.db.{Album, Artist, Song}
import model.{AlbumCrawlField, ArtistCrawlField, SongCrawlField}
import play.api.mvc._
import util.crawl.{AutoIfUnique, CrawlHelper}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MusicbrainzCrawler @Inject()(
  musicbrainzAPI: MusicbrainzAPI,
  crawlHelper: CrawlHelper
)(implicit ec: ExecutionContext) extends InjectedController {
  def crawlArtistDetails(artist: Artist) = {
    artist.musicbrainzId match {
      case Some(musicbrainzId) =>
        musicbrainzAPI.getArtist(musicbrainzId).flatMap {
          case Some(mbArtist) =>
            mbArtist.wikidataId match {
              case Some(wikidataId) =>
                println(mbArtist)
                crawlHelper.processArtist(
                  artist = artist,
                  field = ArtistCrawlField.WikidataId,
                  candidateValues = Seq(wikidataId),
                  comment = s"Musicbrainz details ($musicbrainzId)",
                  strategy = AutoIfUnique
                )
              case None =>
                Future.successful(())
            }
          case None =>
            Future.successful(())
        }
      case None =>
        Future.successful(())
    }
  }

  def crawlAlbumDetails(album: Album) = {
    album.musicbrainzId match {
      case Some(musicbrainzId) =>
        musicbrainzAPI.getReleaseGroup(musicbrainzId).flatMap {
          case Some(mbAlbum) =>
            mbAlbum.wikidataId match {
              case Some(wikidataId) =>
                println(mbAlbum)
                crawlHelper.processAlbum(
                  album = album,
                  field = AlbumCrawlField.WikidataId,
                  candidateValues = Seq(wikidataId),
                  comment = s"Musicbrainz details ($musicbrainzId)",
                  strategy = AutoIfUnique
                )
              case None =>
                Future.successful(())
            }
          case None =>
            Future.successful(())
        }
      case None =>
        Future.successful(())
    }
  }

  def crawlSongDetails(song: Song) = {
    song.musicbrainzWorkId match {
      case Some(musicbrainzId) =>
        musicbrainzAPI.getWork(musicbrainzId).flatMap {
          case Some(mbWork) =>
            mbWork.wikidataId match {
              case Some(wikidataId) =>
                println(mbWork)
                crawlHelper.processSong(
                  song = song,
                  field = SongCrawlField.WikidataId,
                  candidateValues = Seq(wikidataId),
                  comment = s"Musicbrainz details ($musicbrainzId)",
                  strategy = AutoIfUnique
                )
              case None =>
                Future.successful(())
            }
          case None =>
            Future.successful(())
        }
      case None =>
        Future.successful(())
    }
  }
}
