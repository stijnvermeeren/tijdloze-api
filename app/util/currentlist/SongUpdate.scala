package util.currentlist

import model.api.Song
import play.api.libs.json.{Json, OWrites}

final case class SongUpdate(
  song: Song
)

object SongUpdate {
  implicit val jsonWrites: OWrites[SongUpdate] = Json.writes[SongUpdate]
}
