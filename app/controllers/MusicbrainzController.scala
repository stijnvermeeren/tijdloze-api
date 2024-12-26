package controllers

import model.AlbumCrawlField
import model.db.dao.{AlbumDAO, ArtistDAO, MBDataDAO, SongDAO}
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.musicbrainz.MusicbrainzAPI

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
  mbDataDAO: MBDataDAO
)(implicit ec: ExecutionContext) extends InjectedController {

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

  def crawlSongs() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      songDAO.getAll().flatMap { songs =>
        FutureUtil.traverseSequentially(songs) { song =>
          for {
            artist <- artistDAO.get(song.artistId)
            album <- albumDAO.get(song.albumId)
            _ = println(s"${artist.name} - ${song.title}")
            matchingRow <- mbDataDAO.searchArtistTitle(artist.name, song.title)
          } yield {
            matchingRow match {
              case Some(row) =>
                if (album.musicbrainzId.contains(row.albumMBId)) {
                  println(s"  OK (${album.musicbrainzId}, ${album.title}, ${album.releaseYear} / ${row.releaseYear})")
                } else {
                  println(s"  MISMATCH!  Old: ${album.musicbrainzId}, ${album.title}.  New: ${row.releaseYear}, ${row.albumTitle})")

                  if (album.musicbrainzId.isEmpty) {
                    crawlHelper.processAlbum(
                      album = album,
                      field = AlbumCrawlField.MusicbrainzId,
                      candidateValues = Seq(row.albumMBId),
                      comment = s"Musicbrainz search (${artist.name} - ${song.title})",
                      strategy = AutoOnlyForExistingValue
                    )
                  }
                }
              case None =>
                println("  no match")
            }
          }
        }
      }.map(_ => Ok)
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
