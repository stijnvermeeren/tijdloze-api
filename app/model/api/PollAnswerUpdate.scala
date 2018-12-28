package model.api

import play.api.libs.json.Json

final case class PollAnswerUpdate(
  answer: String
)

object PollAnswerUpdate {
  implicit val jsonReads = Json.reads[PollAnswerUpdate]
}
