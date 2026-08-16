package model.api

import play.api.libs.json.{Json, Reads}

final case class PollCreate(
  question: String,
  answers: Seq[String],
  year: Int
)

object PollCreate {
  implicit val jsonReads: Reads[PollCreate] = Json.reads[PollCreate]
}
