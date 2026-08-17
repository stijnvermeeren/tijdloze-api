package model.api

import play.api.libs.json.{Json, Reads}

final case class PollAnswerUpdate(
  answer: String
)

object PollAnswerUpdate {
  implicit val jsonReads: Reads[PollAnswerUpdate] = Json.reads[PollAnswerUpdate]
}
