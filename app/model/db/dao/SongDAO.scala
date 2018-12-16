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
      lyrics = lyrics.getOrElse(""),
      languageId = languageId,
      leadVocals = leadVocals,
      notes = notes.getOrElse(""),
      urlWikiEn = urlWikiEn.getOrElse(""),
      urlWikiNl = urlWikiNl.getOrElse(""),
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
          lyrics.getOrElse(""),
          languageId,
          leadVocals,
          notes.getOrElse(""),
          urlWikiEn.getOrElse(""),
          urlWikiNl.getOrElse(""),
          spotifyId.filter(_.nonEmpty),
          DateTime.now()
        ))
    }
  }

  def markExit(songId: SongId): Future[Int] = {
    db run {
      SongTable.filter(_.id === songId).map(_.exitCurrent).update(true)
    }
  }

  def unmarkExit(songId: SongId): Future[Int] = {
    db run {
      SongTable.filter(_.id === songId).map(_.exitCurrent).update(false)
    }
  }

  def unmarkAllExit(): Future[Int] = {
    db run {
      SongTable.map(_.exitCurrent).update(false)
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

  def exits(): Future[Seq[SongId]] = {
    db run {
      SongTable.filter(_.exitCurrent).map(_.id).result
    }
  }
}
