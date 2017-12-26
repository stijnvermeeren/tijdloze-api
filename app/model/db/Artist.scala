package model
package db

final case class Artist(
  id: ArtistId,
  firstName: String,
  name: String,
  countryId: String,
  notes: String,
  urlOfficial: String,
  urlWikiEn: String,
  urlWikiNl: String,
  urlAllmusic: String,
  edit: Boolean
)
