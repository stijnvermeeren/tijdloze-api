package model
package db
package dao
package table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

private[table] trait SongTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class SongTable(tag: Tag) extends Table[Song](tag, "nummer") {
    val id = column[SongId]("id", O.AutoInc, O.PrimaryKey)
    val artistId = column[ArtistId]("artiest_id")
    val albumId = column[AlbumId]("album_id")
    val title = column[String]("titel")
    val lyrics = column[String]("lyrics")
    val languageId = column[String]("taal_afkorting")
    val leadVocals = column[String]("lead_vocals")
    val notes = column[String]("opmerkingen")
    val urlWikiEn = column[String]("url_wikien")
    val urlWikiNl = column[String]("url_wikinl")
    val spotifyId = column[Option[String]]("spotify_id")
    val edit = column[Boolean]("edit")
    val lastUpdate = column[DateTime]("last_update")
    val exitCurrent = column[Boolean]("exit_huidige")

    def * = (id, artistId, albumId, title, exitCurrent, lyrics, languageId, leadVocals, notes, urlWikiEn, urlWikiNl, spotifyId, edit, lastUpdate) <>
      ((Song.apply _).tupled, Song.unapply)
  }

  val SongTable = TableQuery[SongTable]
}
