package controllers

import model.AlbumCrawlField
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlAlbumDAO}
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import util.FutureUtil
import util.coverartarchive.CoverArtArchiveAPI
import util.crawl.{AutoIfUnique, CrawlHelper}
import util.musicbrainz.MusicbrainzAPI

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CoverArtArchiveController @Inject()(
  coverArtArchiveAPI: CoverArtArchiveAPI,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  crawlAlbumDAO: CrawlAlbumDAO,
  crawlHelper: CrawlHelper,
  cache: AsyncCacheApi
) extends InjectedController {

  def crawlAlbums() = {
    Action.async { implicit request =>
      albumDAO.getAll().flatMap{ albums =>
        val albumsWithMusicbrainzId = albums.flatMap { album =>
          album.musicbrainzId.map(musicbrainzId => (album, musicbrainzId))
        }
        FutureUtil.traverseSequentially(albumsWithMusicbrainzId) { case (album, musicbrainzId) =>
          coverArtArchiveAPI.searchAlbum(musicbrainzId) flatMap {
            case Some(coverName) =>
              for {
                _ <- crawlHelper.processAlbum(
                  album = album,
                  field = AlbumCrawlField.Cover,
                  candidateValues = Seq(coverName),
                  comment = s"CoverArtArchive search ($musicbrainzId)",
                  strategy = AutoIfUnique
                )
              } yield Thread.sleep(1000)
            case None =>
              Thread.sleep(1000)
              Future.successful(())
          }
        }
      } map { _ =>
        cache.remove("coreData")
        Ok
      }
    }
  }
}
