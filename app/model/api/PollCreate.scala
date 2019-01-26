package model.api

import play.api.libs.json.Json

final case class PollCreate(
  question: String,
  answers: Seq[String],
  year: Int
)

object PollCreate {
  implicit val jsonReads = Json.reads[PollCreate]
}
