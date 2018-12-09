package model
package api

import play.api.libs.json.Json

final case class Artist(
  id: ArtistId,
  firstName: String,
  name: String,
  countryId: String,
  notes: String,
  urlOfficial: String,
  urlWikiEn: String,
  urlWikiNl: String,
  urlAllMusic: String
)

object Artist {
  def fromDb(dbArtist: db.Artist): Artist = {
    Artist(
      id = dbArtist.id,
      firstName = dbArtist.firstName,
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
