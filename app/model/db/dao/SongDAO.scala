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

  def create(data: SongSave): Future[Song] = {
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
      (SongTable returning SongTable) += newSong
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
}
