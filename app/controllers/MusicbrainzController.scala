package controllers

import model.api.MusicbrainzResult
import model.{AlbumCrawlField, ArtistCrawlField}
import model.db.dao.{AlbumDAO, ArtistDAO, CrawlAlbumDAO, CrawlArtistDAO, MBDataDAO, SongDAO}
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.coverartarchive.CoverArtArchiveAPI
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}
import util.musicbrainz.{AWSAthena, MusicbrainzAPI}
import util.spotify.SpotifyAPI

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
  crawlHelper: CrawlHelper,
  mbDataDAO: MBDataDAO
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
        FutureUtil.traverseSequentially(songs.filter(_.id.value == 2709)) { song =>
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
