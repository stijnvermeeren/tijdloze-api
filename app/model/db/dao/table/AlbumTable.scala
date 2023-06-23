package model
package db
package dao
package table
import slick.jdbc.MySQLProfile.api._

class AlbumTable(tag: Tag) extends Table[Album](tag, "album") {
  val id = column[AlbumId]("id", O.AutoInc, O.PrimaryKey)
  val artistId = column[ArtistId]("artist_id")
  val title = column[String]("title")
  val releaseYear = column[Int]("release_year")
  val urlWikiEn = column[Option[String]]("url_wikien")
  val urlWikiNl = column[Option[String]]("url_wikinl")
  val urlAllMusic = column[Option[String]]("url_allmusic")
  val spotifyId = column[Option[String]]("spotify_id")
  val wikidataId = column[Option[String]]("wikidata_id")
  val musicbrainzId = column[Option[String]]("musicbrainz_id")
  val cover = column[Option[String]]("cover")

  def * = (id, artistId, title, releaseYear, urlWikiEn, urlWikiNl, urlAllMusic, spotifyId, wikidataId, musicbrainzId, cover) <>
    ((Album.apply _).tupled, Album.unapply)
}
