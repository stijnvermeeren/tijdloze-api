package model.api

import play.api.libs.json.Json

final case class CommentSave(
  message: String
)

object CommentSave {
  implicit val jsonReads = Json.reads[CommentSave]
}






