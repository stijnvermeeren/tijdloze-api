package model.api

import play.api.libs.json.Json

final case class PollCreate(
  question: String,
  year: Int,
  answers: Seq[String],
  isActive: Boolean = true,
  isDeleted: Boolean = true
)

object PollCreate {
  implicit val jsonReads = Json.reads[PollCreate]
}
