package model.api

import play.api.libs.json.Json

final case class SpotifyHit(
  spotifyId: String,
  title: String,
  artist: String,
  album: String,
  year: Int
)

object SpotifyHit {
  implicit val jsonWrites = Json.writes[SpotifyHit]
}

