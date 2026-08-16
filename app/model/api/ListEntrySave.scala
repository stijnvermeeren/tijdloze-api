package model.api

import model.SongId
import play.api.libs.json.{Json, Reads}

final case class ListEntrySave(
  songId: SongId
)

object ListEntrySave {
  implicit val jsonReads: Reads[ListEntrySave] = Json.reads[ListEntrySave]
}
