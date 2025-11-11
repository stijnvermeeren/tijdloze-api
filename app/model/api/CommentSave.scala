package model.api

import model.CommentId
import play.api.libs.json.Json

final case class CommentSave(
  message: String,
  parentId: Option[CommentId]
)

object CommentSave {
  implicit val jsonReads = Json.reads[CommentSave]
}
