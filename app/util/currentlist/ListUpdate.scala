package util.currentlist

import model.SongId
import play.api.libs.json.Json

final case class ListUpdate(
  year: Int,
  entries: Seq[ListUpdateEntry],
  exitSongIds: Seq[SongId]
)

object ListUpdate {
  implicit val jsonWrites = Json.writes[ListUpdate]
}
