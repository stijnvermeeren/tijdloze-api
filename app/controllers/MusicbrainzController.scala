package controllers

import model.AlbumCrawlField
import model.db.dao.{AlbumDAO, ArtistDAO, MBDataDAO, SongDAO}
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, CrawlHelper}
import util.musicbrainz.{MusicbrainzAPI, MusicbrainzCrawler}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MusicbrainzController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  musicbrainzAPI: MusicbrainzAPI,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  songDAO: SongDAO,
  crawlHelper: CrawlHelper,
  mbDataDAO: MBDataDAO,
  musicbrainzCrawler: MusicbrainzCrawler
)(implicit ec: ExecutionContext) extends InjectedController {
  def crawlArtistDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        FutureUtil.traverseSequentially(artists)(musicbrainzCrawler.crawlArtistDetails)
      } map { _ =>
        Ok
      }
    }
  }

  def crawlAlbumDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      albumDAO.getAll().flatMap{ albums =>
        FutureUtil.traverseSequentially(albums) { album =>
          artistDAO.get(album.artistId) flatMap { artist =>
            musicbrainzCrawler.crawlAlbumDetails(album, artist)
          }
        }
      } map { _ =>
        Ok
      }
    }
  }

  def crawlSongDetails() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      songDAO.getAll().flatMap{ songs =>
        FutureUtil.traverseSequentially(songs)(musicbrainzCrawler.crawlSongDetails)
      } map { _ =>
        Ok
      }
    }
  }

  def crawlAlbums() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      albumDAO.getAll().flatMap{ albums =>
        FutureUtil.traverseSequentially(albums) { album =>
          artistDAO.get(album.artistId) flatMap { artist =>
            musicbrainzAPI.searchAlbum(album, artist) flatMap { releaseGroups =>
              for {
                _ <- crawlHelper.processAlbum(
                  album = album,
                  field = AlbumCrawlField.MusicbrainzId,
                  candidateValues = releaseGroups.map(_.id),
                  comment = s"Musicbrainz search (${artist.musicbrainzId.getOrElse(artist.name)})",
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


  def find() = {
    Action.async { implicit request =>
      (request.getQueryString("artist"), request.getQueryString("title")) match {
        case (Some(artist), Some(title)) =>
          for {
            matchingRow <- mbDataDAO.searchArtistTitle(artist, title)
          } yield {
            Ok(matchingRow.toString)
          }
        case _ =>
          Future.successful(BadRequest)
      }
    }
  }
}
