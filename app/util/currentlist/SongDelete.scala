package util.currentlist

import model.SongId
import play.api.libs.json.Json

final case class SongDelete(
  deletedSongId: SongId
)

object SongDelete {
  implicit val jsonWrites = Json.writes[SongDelete]
}
