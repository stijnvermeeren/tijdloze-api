package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.SongSave
import model.db.dao.table.SongTable
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

@Singleton
class SongDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val songTable = TableQuery[SongTable]

  def get(songId: SongId): Future[Song] = {
    db run {
      songTable.filter(_.id === songId).result.head
    }
  }

  def getAll(): Future[Seq[Song]] = {
    db run {
      songTable.result
    }
  }

  def create(data: SongSave): Future[SongId] = {
    import data._
    val newSong = Song(
      artistId = artistId,
      secondArtistId = secondArtistId,
      albumId = albumId,
      title = title,
      aliases = aliases.map(_.trim).filter(_.nonEmpty),
      lyrics = lyrics.map(_.trim).filter(_.nonEmpty),
      languageId = languageId.map(_.trim).filter(_.nonEmpty),
      leadVocals = leadVocals.map(_.trim).filter(_.nonEmpty),
      notes = notes.map(_.trim).filter(_.nonEmpty),
      musicbrainzRecordingId = musicbrainzRecordingId.map(_.trim).filter(_.nonEmpty),
      musicbrainzWorkId = musicbrainzWorkId.map(_.trim).filter(_.nonEmpty),
      wikidataId = wikidataId.map(_.trim).filter(_.nonEmpty),
      urlWikiEn = urlWikiEn.map(_.trim).filter(_.nonEmpty),
      urlWikiNl = urlWikiNl.map(_.trim).filter(_.nonEmpty),
      spotifyId = spotifyId.map(_.trim).filter(_.nonEmpty)
    )

    db run {
      (songTable returning songTable.map(_.id)) += newSong
    }
  }

  def update(songId: SongId, data: SongSave): Future[Int] = {
    import data._

    db run {
      songTable
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
          x.musicbrainzRecordingId,
          x.musicbrainzWorkId,
          x.wikidataId,
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
          musicbrainzRecordingId.map(_.trim).filter(_.nonEmpty),
          musicbrainzWorkId.map(_.trim).filter(_.nonEmpty),
          wikidataId.map(_.trim).filter(_.nonEmpty),
          urlWikiEn.map(_.trim).filter(_.nonEmpty),
          urlWikiNl.map(_.trim).filter(_.nonEmpty),
          spotifyId.map(_.trim).filter(_.nonEmpty),
          DateTime.now()
        ))
    }
  }

  def delete(songId: SongId): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).delete
    }
  }

  def setUrlWikiEn(songId: SongId, url: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.urlWikiEn).update(url)
    }
  }

  def setUrlWikiNl(songId: SongId, url: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.urlWikiNl).update(url)
    }
  }

  def setLanguageId(songId: SongId, languageId: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.languageId).update(languageId)
    }
  }

  def setSpotifyId(songId: SongId, spotifyId: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.spotifyId).update(spotifyId)
    }
  }

  def setWikidataId(songId: SongId, wikidataId: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.wikidataId).update(wikidataId)
    }
  }

  def setMusicbrainzRecordingId(songId: SongId, musicbrainzRecordingId: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.musicbrainzRecordingId).update(musicbrainzRecordingId)
    }
  }

  def setMusicbrainzWorkId(songId: SongId, musicbrainzWorkId: Option[String]): Future[Int] = {
    db run {
      songTable.filter(_.id === songId).map(_.musicbrainzWorkId).update(musicbrainzWorkId)
    }
  }
}
