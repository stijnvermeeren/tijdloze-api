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
    val secondArtistId = column[Option[ArtistId]]("second_artist_id")
    val albumId = column[AlbumId]("album_id")
    val title = column[String]("title")
    val aliases = column[Option[String]]("aliases")
    val lyrics = column[Option[String]]("lyrics")
    val languageId = column[Option[String]]("language_id")
    val leadVocals = column[Option[String]]("lead_vocals_id")
    val notes = column[Option[String]]("notes")
    val urlWikiEn = column[Option[String]]("url_wikien")
    val urlWikiNl = column[Option[String]]("url_wikinl")
    val spotifyId = column[Option[String]]("spotify_id")
    val lastUpdate = column[DateTime]("last_update")

    def * = (id, artistId, secondArtistId, albumId, title, aliases, lyrics, languageId, leadVocals, notes, urlWikiEn, urlWikiNl, spotifyId, lastUpdate) <>
      ((Song.apply _).tupled, Song.unapply)
  }

  val SongTable = TableQuery[SongTable]
}
