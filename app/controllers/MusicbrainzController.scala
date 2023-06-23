package controllers

import model.{AlbumCrawlField, ArtistCrawlField}
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlAlbumDAO, CrawlArtistDAO, SongDAO}
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.musicbrainz.MusicbrainzAPI
import util.wikidata.WikidataAPI

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MusicbrainzController @Inject()(
  musicbrainzAPI: MusicbrainzAPI,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  crawlAlbumDAO: CrawlAlbumDAO,
  crawlHelper: CrawlHelper,
  cache: AsyncCacheApi
) extends InjectedController {

  def crawlAlbums() = {
    Action.async { implicit request =>
      albumDAO.getAll().flatMap{ albums =>
        FutureUtil.traverseSequentially(albums) { album =>
          artistDAO.get(album.artistId) flatMap { artist =>
            musicbrainzAPI.searchAlbum(album, artist) flatMap { releaseGroups =>
              for {
                _ <- crawlHelper.processAlbum(
                  album = album,
                  field = AlbumCrawlField.MusicbrainzId,
                  candidateValues = releaseGroups.map(_.id),
                  comment = s"Musicbrainz search (${artist.musicbrainzId.getOrElse(artist.fullName)})",
                  strategy = AutoIfUnique
                )
              } yield Thread.sleep(1000)
            }
          }
        }
      } map { _ =>
        Ok
      }
    }
  }
}
