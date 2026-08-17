package model.api

import play.api.libs.json.{Json, Reads}

final case class PollUpdate(
  question: String
)

object PollUpdate {
  implicit val jsonReads: Reads[PollUpdate] = Json.reads[PollUpdate]
}
