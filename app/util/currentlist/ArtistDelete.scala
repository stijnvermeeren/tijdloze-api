package util.currentlist

import model.ArtistId
import play.api.libs.json.Json

final case class ArtistDelete(
  deletedArtistId: ArtistId
)

object ArtistDelete {
  implicit val jsonWrites = Json.writes[ArtistDelete]
}
