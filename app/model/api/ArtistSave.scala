package model.api

import play.api.libs.json.Json

final case class ArtistSave(
  namePrefix: Option[String],
  name: String,
  countryId: String,
  notes: Option[String],
  urlOfficial: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String],
  spotifyId: Option[String]
)

object ArtistSave {
  implicit val jsonReads = Json.reads[ArtistSave]
}
