package model
package api

import play.api.libs.json.Json

final case class Artist(
  id: ArtistId,
  namePrefix: Option[String],
  name: String,
  countryId: String,
  notes: Option[String],
  urlOfficial: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String]
)

object Artist {
  def fromDb(dbArtist: db.Artist): Artist = {
    Artist(
      id = dbArtist.id,
      namePrefix = dbArtist.namePrefix,
      name = dbArtist.name,
      countryId = dbArtist.countryId,
      notes = dbArtist.notes,
      urlOfficial = dbArtist.urlOfficial,
      urlWikiEn = dbArtist.urlWikiEn,
      urlWikiNl = dbArtist.urlWikiNl,
      urlAllMusic = dbArtist.urlAllMusic
    )
  }

  implicit val jsonWrites = Json.writes[Artist]
}
