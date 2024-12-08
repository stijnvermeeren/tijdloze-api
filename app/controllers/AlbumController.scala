package controllers

import javax.inject._
import model.{AlbumCrawlField, AlbumId}
import model.api.{Album, AlbumSave}
import model.db.dao.{AlbumDAO, ArtistDAO}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.coverartarchive.CoverArtArchiveAPI
import util.crawl.{AutoIfUnique, CrawlHelper}
import util.currentlist.CurrentListUtil
import util.musicbrainz.MusicbrainzAPI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AlbumController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  dataCache: DataCache,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  musicbrainzAPI: MusicbrainzAPI,
  coverArtArchiveAPI: CoverArtArchiveAPI,
  crawlHelper: CrawlHelper,
  currentList: CurrentListUtil
) extends InjectedController {

  def get(albumId: AlbumId) = {
    Action.async { implicit rs =>
      dataCache.AlbumDataCache.load(albumId)
    }
  }

  def getByMusicbrainzId(musicbrainzId: String) = {
    Action.async { implicit rs =>
      for {
        album <- albumDAO.getByMusicbrainzId(musicbrainzId)
      } yield Ok(Json.toJson(Album.fromDb(album)))
    }
  }

  def post() = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[AlbumSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        albumSave => {
          for {
            newAlbumId <- albumDAO.create(albumSave)
            newAlbum <- albumDAO.get(newAlbumId)
          } yield {
            dataCache.CoreDataCache.reload()
            dataCache.AlbumDataCache.reload(newAlbumId)
            currentList.updateAlbum(Album.fromDb(newAlbum))

            checkMusicbrainz(newAlbum)

            Ok(Json.toJson(Album.fromDb(newAlbum)))
          }
        }
      )
    }
  }

  def put(albumId: AlbumId) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[AlbumSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        albumSave => {
          for {
            _ <- albumDAO.update(albumId, albumSave)
            album <- albumDAO.get(albumId)
          } yield {
            dataCache.CoreDataCache.reload()
            dataCache.AlbumDataCache.reload(albumId)
            currentList.updateAlbum(Album.fromDb(album))

            checkMusicbrainz(album)

            Ok(Json.toJson(Album.fromDb(album)))
          }
        }
      )
    }
  }

  private def checkMusicbrainz(album: model.db.Album): Unit = {
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
        } yield ()
      } flatMap { _ =>
        albumDAO.get(album.id).map(_.musicbrainzId) flatMap {
          case Some(musicbrainzId) =>
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
                } yield ()
              case None =>
                Future.successful(())
            }
          case None =>
            Future.successful(())
        }
      }
    } flatMap { _ =>
      albumDAO.get(album.id) map { album =>
        dataCache.CoreDataCache.reload()
        dataCache.AlbumDataCache.reload(album.id)
        currentList.updateAlbum(Album.fromDb(album))
      }
    }
  }

  def delete(albumId: AlbumId) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      for {
        _ <- albumDAO.delete(albumId)
      } yield {
        dataCache.CoreDataCache.reload()
        dataCache.AlbumDataCache.remove(albumId)
        currentList.deleteAlbum(albumId)
        Ok("")
      }
    }
  }
}
