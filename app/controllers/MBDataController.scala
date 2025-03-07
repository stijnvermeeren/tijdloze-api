package controllers

import model.{AlbumCrawlField, SongCrawlField}
import model.SongCrawlField.LanguageId
import model.api.MBDatasetResponse
import model.db.dao.{AlbumDAO, ArtistDAO, MBDataDAO, SongDAO}
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.crawl.{AutoIfUnique, AutoOnlyForExistingValue, CrawlHelper}

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class MBDataController @Inject()(
  authenticateAdmin: AuthenticateAdmin,
  mbDataDAO: MBDataDAO,
  songDAO: SongDAO,
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  crawlHelper: CrawlHelper
)(implicit ec: ExecutionContext) extends InjectedController {
  def search(artistQuery: String, titleQuery: String) = {
    Action.async { implicit request =>
      mbDataDAO.searchArtistTitle(artistQuery, titleQuery) map { hit =>
        Ok(Json.toJson(MBDatasetResponse(hit)))
      }
    }
  }

  def searchQuery(query: String) = {
    Action.async { implicit request =>
      mbDataDAO.searchQuery(query) map { hit =>
        Ok(Json.toJson(MBDatasetResponse(hit)))
      }
    }
  }

  def crawlSongs() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      songDAO.getAll().flatMap { songs =>
        FutureUtil.traverseSequentially(songs) { song =>
          println(song.title)
          for {
            artist <- artistDAO.get(song.artistId)
            album <- albumDAO.get(song.albumId)
            _ = println(s"${artist.name} - ${song.title}")
            matchingRow <- mbDataDAO.searchArtistTitle(artist.name, song.title)
          } yield {
            matchingRow match {
              case Some(row) =>
                val languages = row.language.toSeq map { languageId =>
                  if (languageId == "zxx") "i" else languageId
                }
                crawlHelper.processSong(
                  song = song,
                  field = SongCrawlField.LanguageId,
                  candidateValues = languages,
                  comment = s"Musicbrainz export (${artist.name} - ${song.title})",
                  strategy = AutoOnlyForExistingValue
                )
                crawlHelper.processSong(
                  song = song,
                  field = SongCrawlField.MusicbrainzWorkId,
                  candidateValues = row.workMBId.toSeq,
                  comment = s"Musicbrainz export (${artist.name} - ${song.title})",
                  strategy = AutoIfUnique
                )

                if (album.musicbrainzId.contains(row.albumMBId)) {
                  crawlHelper.processSong(
                    song = song,
                    field = SongCrawlField.MusicbrainzRecordingId,
                    candidateValues = Seq(row.recordingMBId),
                    comment = s"Musicbrainz export (${artist.name} - ${song.title})",
                    strategy = AutoIfUnique
                  )
                  println(s"  OK (${album.musicbrainzId}, ${album.title}, ${album.releaseYear} / ${row.releaseYear})")
                } else {
                  println(s"  MISMATCH!  Old: ${album.musicbrainzId}, ${album.title}.  New: ${row.releaseYear}, ${row.albumTitle})")

                  crawlHelper.processAlbum(
                    album = album,
                    field = AlbumCrawlField.MusicbrainzId,
                    candidateValues = Seq(row.albumMBId),
                    comment = s"Musicbrainz search (${artist.name} - ${song.title})",
                    strategy = AutoOnlyForExistingValue
                  )
                }
              case None =>
                println("  no match")
            }
          }
        }
      }.map(_ => Ok)
    }
  }
}
