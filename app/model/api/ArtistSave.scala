package model.api

import play.api.libs.json.Json

final case class ArtistSave(
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

object ArtistSave {
  implicit val jsonReads = Json.reads[ArtistSave]
}
