package model
package db
package dao
package table
import slick.jdbc.MySQLProfile.api._

class ArtistTable(tag: Tag) extends Table[Artist](tag, "artist") {
  val id = column[ArtistId]("id", O.AutoInc, O.PrimaryKey)
  val namePrefix = column[Option[String]]("name_prefix")
  val name = column[String]("name")
  val aliases = column[Option[String]]("aliases")
  val countryId = column[Option[String]]("country_id")
  val notes = column[Option[String]]("notes")
  val urlOfficial = column[Option[String]]("url_official")
  val urlWikiEn = column[Option[String]]("url_wikien")
  val urlWikiNl = column[Option[String]]("url_wikinl")
  val urlAllMusic = column[Option[String]]("url_allmusic")
  val spotifyId = column[Option[String]]("spotify_id")
  val wikidataId = column[Option[String]]("wikidata_id")
  val musicbrainzId = column[Option[String]]("musicbrainz_id")

  def * = (id, namePrefix, name, aliases, countryId, notes, urlOfficial, urlWikiEn, urlWikiNl, urlAllMusic, spotifyId, wikidataId, musicbrainzId) <>
    ((Artist.apply _).tupled, Artist.unapply)
}
