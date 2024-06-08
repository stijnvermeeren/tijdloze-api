package model
package db

final case class Artist(
  id: ArtistId = ArtistId(0),
  name: String,
  aliases: Option[String],
  countryId: Option[String],
  notes: Option[String],
  urlOfficial: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String],
  spotifyId: Option[String],
  wikidataId: Option[String],
  musicbrainzId: Option[String]
)
