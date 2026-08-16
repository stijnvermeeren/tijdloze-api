package util.currentlist

import model.SongId
import play.api.libs.json.{Json, OWrites}

final case class CurrentYearUpdate(
  currentYear: Int,
  exitSongIds: Seq[SongId]
)

object CurrentYearUpdate {
  implicit val jsonWrites: OWrites[CurrentYearUpdate] = Json.writes[CurrentYearUpdate]
}
