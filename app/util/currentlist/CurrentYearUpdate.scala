package util.currentlist

import model.SongId
import play.api.libs.json.Json

final case class CurrentYearUpdate(
  currentYear: Int,
  exitSongIds: Seq[SongId]
)

object CurrentYearUpdate {
  implicit val jsonWrites = Json.writes[CurrentYearUpdate]
}
