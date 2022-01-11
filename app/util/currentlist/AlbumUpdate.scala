package util.currentlist

import model.api.Album
import play.api.libs.json.Json

final case class AlbumUpdate(
  album: Album
)

object AlbumUpdate {
  implicit val jsonWrites = Json.writes[AlbumUpdate]
}
