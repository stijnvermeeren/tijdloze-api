package model
package db
package dao
package table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

private[table] trait SongTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class SongTable(tag: Tag) extends Table[Song](tag, "song") {
    val id = column[SongId]("id", O.AutoInc, O.PrimaryKey)
    val artistId = column[ArtistId]("artist_id")
    val albumId = column[AlbumId]("album_id")
    val title = column[String]("title")
    val lyrics = column[String]("lyrics")
    val languageId = column[String]("language_id")
    val leadVocals = column[String]("lead_vocals_id")
    val notes = column[Option[String]]("notes")
    val urlWikiEn = column[Option[String]]("url_wikien")
    val urlWikiNl = column[Option[String]]("url_wikinl")
    val spotifyId = column[Option[String]]("spotify_id")
    val lastUpdate = column[DateTime]("last_update")

    def * = (id, artistId, albumId, title, lyrics, languageId, leadVocals, notes, urlWikiEn, urlWikiNl, spotifyId, lastUpdate) <>
      ((Song.apply _).tupled, Song.unapply)
  }

  val SongTable = TableQuery[SongTable]
}
