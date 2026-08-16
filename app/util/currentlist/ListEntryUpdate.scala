package util.currentlist

import model.SongId
import play.api.libs.json.{Json, OWrites}

final case class ListEntryUpdate(
  year: Int,
  position: Int,
  songId: Option[SongId]
)

object ListEntryUpdate {
  implicit val jsonWrites: OWrites[ListEntryUpdate] = Json.writes[ListEntryUpdate]
}
