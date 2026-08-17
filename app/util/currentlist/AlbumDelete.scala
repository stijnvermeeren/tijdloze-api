package util.currentlist

import model.AlbumId
import play.api.libs.json.{Json, OWrites}

final case class AlbumDelete(
  deletedAlbumId: AlbumId
)

object AlbumDelete {
  implicit val jsonWrites: OWrites[AlbumDelete] = Json.writes[AlbumDelete]
}
