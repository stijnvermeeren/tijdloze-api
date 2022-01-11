package util.currentlist

import model.SongId
import play.api.libs.json.Json

final case class ListEntryUpdate(
  year: Int,
  position: Int,
  songId: Option[SongId]
)

object ListEntryUpdate {
  implicit val jsonWrites = Json.writes[ListEntryUpdate]
}
