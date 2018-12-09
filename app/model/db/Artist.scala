package model
package db

final case class Artist(
  id: ArtistId = ArtistId(0),
  firstName: String,
  name: String,
  countryId: String,
  notes: String,
  urlOfficial: String,
  urlWikiEn: String,
  urlWikiNl: String,
  urlAllMusic: String,
  edit: Boolean = false
)
