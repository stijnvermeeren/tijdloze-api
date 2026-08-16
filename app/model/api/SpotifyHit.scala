package model.api

import play.api.libs.json.{Json, OWrites}

final case class SpotifyHit(
  spotifyId: String,
  title: String,
  artist: String,
  album: String,
  year: Int
)

object SpotifyHit {
  implicit val jsonWrites: OWrites[SpotifyHit] = Json.writes[SpotifyHit]
}

