package util.currentlist

import model.api.Album
import play.api.libs.json.{Json, OWrites}

final case class AlbumUpdate(
  album: Album
)

object AlbumUpdate {
  implicit val jsonWrites: OWrites[AlbumUpdate] = Json.writes[AlbumUpdate]
}
