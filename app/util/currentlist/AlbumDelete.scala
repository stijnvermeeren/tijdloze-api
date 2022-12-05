package util.currentlist

import model.AlbumId
import play.api.libs.json.Json

final case class AlbumDelete(
  deletedAlbumId: AlbumId
)

object AlbumDelete {
  implicit val jsonWrites = Json.writes[AlbumDelete]
}
