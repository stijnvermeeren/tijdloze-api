package util.currentlist

import model.api.Artist
import play.api.libs.json.Json

final case class ArtistUpdate(
  artist: Artist
)

object ArtistUpdate {
  implicit val jsonWrites = Json.writes[ArtistUpdate]
}
