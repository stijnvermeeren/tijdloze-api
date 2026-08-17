package util.currentlist

import model.SongId
import play.api.libs.json.{Json, OWrites}

final case class SongDelete(
  deletedSongId: SongId
)

object SongDelete {
  implicit val jsonWrites: OWrites[SongDelete] = Json.writes[SongDelete]
}
