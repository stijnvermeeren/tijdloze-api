package controllers

import model.api.MusicbrainzResult
import model.{AlbumCrawlField, ArtistCrawlField}
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlAlbumDAO, CrawlArtistDAO, SongDAO}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.coverartarchive.CoverArtArchiveAPI
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.musicbrainz.{AWSAthena, MusicbrainzAPI}
import util.spotify.SpotifyAPI
import util.wikidata.WikidataAPI

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MusicbrainzController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  spotifyAPI: SpotifyAPI,
  musicbrainzAPI: MusicbrainzAPI,
  coverArtArchiveAPI: CoverArtArchiveAPI,
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
            query = s"${artist.name} ${song.title}"
            _ = println(s"${artist.name} - ${song.title}")
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


  def find() = {
    (Action).async { implicit request =>
      request.getQueryString("query") match {
        case Some(query) =>

          Future { awsAthena.search (query).headOption } flatMap {
            case Some(matchingRow) =>
              for {
                release <- musicbrainzAPI.getRelease(matchingRow.releaseId)
                recording <- musicbrainzAPI.getRecording(matchingRow.recordingId)
                artist <- recording.flatMap(_.artistMusicbrainzId) match {
                  case Some(artistId) => musicbrainzAPI.getArtist(artistId)
                  case None => Future.successful(None)
                }
                cover <- release.map(_.releaseGroupId) match {
                  case Some(releaseGroupId) => coverArtArchiveAPI.searchAlbum(releaseGroupId)
                  case None => Future.successful(None)
                }
                spotifyToken <- spotifyAPI.getToken()
                spotifyResult <- spotifyAPI.findNewSong(spotifyToken, query, limit = 1)
              } yield {
                Ok(Json.toJson(MusicbrainzResult.fromData(matchingRow, release, recording, artist, cover, spotifyResult.headOption)))
              }
            case None =>
              Future.successful(Ok(Json.toJson(MusicbrainzResult.empty)))
          }
        case None =>
          Future.successful(BadRequest("No query specified."))
      }
    }
  }
}
