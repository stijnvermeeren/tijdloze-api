package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.SongSave
import model.db.dao.table.AllTables
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.concurrent.Future

@Singleton
class SongDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(songId: SongId): Future[Song] = {
    db run {
      SongTable.filter(_.id === songId).result.head
    }
  }

  def getAll(): Future[Seq[Song]] = {
    db run {
      SongTable.result
    }
  }

  def setSpotifyId(songId: SongId, spotifyId: Option[String]): Future[Int] = {
    db run {
      SongTable.filter(_.id === songId).map(_.spotifyId).update(spotifyId)
    }
  }

  def create(data: SongSave): Future[SongId] = {
    import data._
    val newSong = Song(
      artistId = artistId,
      albumId = albumId,
      title = title,
      lyrics = lyrics.map(_.trim).filter(_.nonEmpty),
      languageId = languageId.map(_.trim).filter(_.nonEmpty),
      leadVocals = leadVocals.map(_.trim).filter(_.nonEmpty),
      notes = notes.map(_.trim).filter(_.nonEmpty),
      urlWikiEn = urlWikiEn.map(_.trim).filter(_.nonEmpty),
      urlWikiNl = urlWikiNl.map(_.trim).filter(_.nonEmpty),
      spotifyId = spotifyId.map(_.trim).filter(_.nonEmpty)
    )

    db run {
      (SongTable returning SongTable.map(_.id)) += newSong
    }
  }

  def update(songId: SongId, data: SongSave): Future[Int] = {
    import data._

    db run {
      SongTable
        .filter(_.id === songId)
        .map(x => (
          x.artistId,
          x.albumId,
          x.title,
          x.lyrics,
          x.languageId,
          x.leadVocals,
          x.notes,
          x.urlWikiEn,
          x.urlWikiNl,
          x.spotifyId,
          x.lastUpdate)
        )
        .update((
          artistId,
          albumId,
          title,
          lyrics.map(_.trim).filter(_.nonEmpty),
          languageId.map(_.trim).filter(_.nonEmpty),
          leadVocals.map(_.trim).filter(_.nonEmpty),
          notes.map(_.trim).filter(_.nonEmpty),
          urlWikiEn.map(_.trim).filter(_.nonEmpty),
          urlWikiNl.map(_.trim).filter(_.nonEmpty),
          spotifyId.map(_.trim).filter(_.nonEmpty),
          DateTime.now()
        ))
    }
  }

  def newSongs(year: Int): Future[Seq[Song]] = {
    def isNew(songId: Rep[SongId]): Rep[Boolean] = {
      val entryYears = ListEntryTable.filter(_.songId === songId).map(_.year)
      (entryYears.min === year).ifNull(false)
    }

    db run {
      SongTable.filter(song => isNew(song.id)).result
    }
  }
}
