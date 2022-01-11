package util.currentlist

import model.api.Song
import play.api.libs.json.Json

final case class SongUpdate(
  song: Song
)

object SongUpdate {
  implicit val jsonWrites = Json.writes[SongUpdate]
}
