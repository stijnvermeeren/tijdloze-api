package model
package api

import play.api.libs.json.Json

final case class Artist(
  id: ArtistId,
  namePrefix: Option[String],
  name: String,
  aliases: Option[String],
  countryId: Option[String],
  notes: Option[String],
  urlOfficial: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String],
  spotifyId: Option[String]
)

object Artist {
  def fromDb(dbArtist: db.Artist): Artist = {
    Artist(
      id = dbArtist.id,
      namePrefix = dbArtist.namePrefix,
      name = dbArtist.name,
      aliases = dbArtist.aliases,
      countryId = dbArtist.countryId,
      notes = dbArtist.notes,
      urlOfficial = dbArtist.urlOfficial,
      urlWikiEn = dbArtist.urlWikiEn,
      urlWikiNl = dbArtist.urlWikiNl,
      urlAllMusic = dbArtist.urlAllMusic,
      spotifyId = dbArtist.spotifyId
    )
  }

  implicit val jsonWrites = Json.writes[Artist]
}
