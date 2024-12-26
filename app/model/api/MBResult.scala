package model.api

import play.api.libs.json.Json

case class MBDatasetHit(
  songMBId: String,
  matchedAlias: String,
  title: String,
  albumTitle: String,
  releaseYear: Int,
  isSingle: Boolean,
  isSoundtrack: Boolean,
  albumMBId: String,
  name: String,
  artistMBId: String,
  countryId: String,
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

