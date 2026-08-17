package util.currentlist

import model.ArtistId
import play.api.libs.json.{Json, OWrites}

final case class ArtistDelete(
  deletedArtistId: ArtistId
)

object ArtistDelete {
  implicit val jsonWrites: OWrites[ArtistDelete] = Json.writes[ArtistDelete]
}
