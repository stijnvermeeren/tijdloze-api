package model.api

import play.api.libs.json.Json

case class MBDatasetHit(
  recordingMBId: String,
  workMBId: Option[String],
  matchedAlias: String,
  title: String,
  language: Option[String],
  albumTitle: String,
  releaseYear: Int,
  isSingle: Boolean,
  isSoundtrack: Boolean,
  albumMBId: String,
  name: String,
  artistMBId: String,
  countryId: String,
  secondArtistName: Option[String],
  secondArtistMBId: Option[String],
  secondArtistCountryId: Option[String],
  score: Double,
)

object MBDatasetHit {
  implicit val jsonWrites = Json.writes[MBDatasetHit]
}


final case class MBDatasetResponse(
  hit: Option[MBDatasetHit]
)

object MBDatasetResponse {
  implicit val jsonWrites = Json.writes[MBDatasetResponse]

  def empty: MBDatasetResponse = MBDatasetResponse(hit = None)
}

