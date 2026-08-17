package util.currentlist

import model.api.Artist
import play.api.libs.json.{Json, OWrites}

final case class ArtistUpdate(
  artist: Artist
)

object ArtistUpdate {
  implicit val jsonWrites: OWrites[ArtistUpdate] = Json.writes[ArtistUpdate]
}
