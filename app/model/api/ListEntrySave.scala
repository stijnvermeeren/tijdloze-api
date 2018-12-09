package model.api

import model.SongId
import play.api.libs.json.Json

final case class ListEntrySave(
  songId: SongId
)

object ListEntrySave {
  implicit val jsonReads = Json.reads[ListEntrySave]
}
