package model.api

import play.api.libs.json.Json

final case class PollUpdate(
  question: String
)

object PollUpdate {
  implicit val jsonReads = Json.reads[PollUpdate]
}
