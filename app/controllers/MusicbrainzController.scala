package controllers

import model.{AlbumCrawlField, ArtistCrawlField}
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlAlbumDAO, CrawlArtistDAO, SongDAO}
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.musicbrainz.{AWSAthena, MusicbrainzAPI}
import util.wikidata.WikidataAPI

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MusicbrainzController @Inject()(
  musicbrainzAPI: MusicbrainzAPI,
  awsAthena: AWSAthena,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  songDAO: SongDAO,
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

  def searchReleaseGroup(query: String) = {
    Action.async { implicit request =>
      Future {
        awsAthena.search(query).headOption
      } flatMap {
        case Some(row) =>
          musicbrainzAPI.getRelease(row.releaseId).map(release => {
            println(release)
            Ok
          })
        case None =>
          Future.successful(Ok)
      }
    }
  }

  def crawlSongs() = {
    Action.async { implicit request =>
      songDAO.getAll().flatMap { songs =>
        FutureUtil.traverseSequentially(songs) { song =>
          for {
            artist <- artistDAO.get(song.artistId)
            album <- albumDAO.get(song.albumId)
            query = s"${artist.fullName} ${song.title}"
            _ = println(s"${artist.fullName} - ${song.title}")
            matchingRow <- Future { awsAthena.search(query).headOption }
            _ <- matchingRow match {
              case Some(row) =>
                musicbrainzAPI.getRelease(row.releaseId).map(release => {
                  if (release.map(_.releaseGroupId) == album.musicbrainzId) {
                    println(s"  OK (${album.musicbrainzId}, ${album.title}, ${album.releaseYear} / ${release.flatMap(_.releaseYear)})")
                  } else {
                    println(s"  MISMATCH!  Old: ${album.musicbrainzId}, ${album.title}.  New: ${release.map(_.releaseGroupId)}, ${release.map(_.title)})")

                    if (album.musicbrainzId.isEmpty) {
                      crawlHelper.processAlbum(
                        album = album,
                        field = AlbumCrawlField.MusicbrainzId,
                        candidateValues = release.map(_.releaseGroupId).toSeq,
                        comment = s"Musicbrainz search ($query)",
                        strategy = AutoOnlyForExistingValue
                      )
                    }
                  }
                })
              case None =>
                println("  no match")
                Future.successful(())
            }
          } yield ()
        }
      }.map(_ => Ok)
    }
  }
}
