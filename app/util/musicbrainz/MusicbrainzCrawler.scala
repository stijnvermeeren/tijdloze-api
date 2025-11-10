package util.musicbrainz

import model.db.{Album, Artist, Song}
import model.{AlbumCrawlField, ArtistCrawlField, SongCrawlField}
import play.api.mvc._
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}

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

  def crawlAlbumDetails(album: Album, artist: Artist) = {
    album.musicbrainzId match {
      case Some(musicbrainzId) =>
        musicbrainzAPI.getReleaseGroup(musicbrainzId).flatMap {
          case Some(mbAlbum) =>
            println(mbAlbum)
            for {
              _ <- mbAlbum.wikidataId match {
                case Some(wikidataId) =>
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
              _ <- {
                val matchingArtist = mbAlbum.artists.find(_.name.toLowerCase == artist.name.toLowerCase)
                val onlyArtist = mbAlbum.artists.headOption.filter(_ => mbAlbum.artists.size == 1)

                matchingArtist.map(mbArtist => {
                  crawlHelper.processArtist(
                    artist = artist,
                    field = ArtistCrawlField.MusicbrainzId,
                    candidateValues = Seq(mbArtist.id),
                    comment = s"Artist id from album ($musicbrainzId)",
                    strategy = AutoIfUnique
                  )
                }) orElse {
                  onlyArtist.map(mbArtist => {
                    crawlHelper.processArtist(
                      artist = artist,
                      field = ArtistCrawlField.MusicbrainzId,
                      candidateValues = Seq(mbArtist.id),
                      comment = s"Artist id from album ($musicbrainzId)",
                      strategy = AutoOnlyForExistingValue
                    )
                  })
                } getOrElse Future.successful(())
              }
            } yield ()

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
