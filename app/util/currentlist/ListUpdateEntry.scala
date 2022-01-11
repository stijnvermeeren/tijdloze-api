package util.currentlist

import model.SongId
import model.db.ListEntry
import play.api.libs.json._

final case class ListUpdateEntry(
  position: Int,
  songId: SongId,
)

object ListUpdateEntry {
  implicit val jsonWrites = Json.writes[ListUpdateEntry]

  def fromDb(listEntry: ListEntry): ListUpdateEntry = {
    ListUpdateEntry(
      position = listEntry.position,
      songId = listEntry.songId
    )
  }
}
