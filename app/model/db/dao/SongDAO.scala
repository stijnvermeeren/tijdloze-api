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

  def create(data: SongSave): Future[SongId] = {
    import data._
    val newSong = Song(
      artistId = artistId,
      secondArtistId = None,
      albumId = albumId,
      title = title,
      aliases = aliases.map(_.trim).filter(_.nonEmpty),
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
          x.secondArtistId,
          x.albumId,
          x.title,
          x.aliases,
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
          secondArtistId,
          albumId,
          title,
          aliases.map(_.trim).filter(_.nonEmpty),
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

  def delete(songId: SongId): Future[Int] = {
    db run {
      SongTable.filter(_.id === songId).delete
    }
  }
}
