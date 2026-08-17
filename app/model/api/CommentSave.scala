package model.api

import model.CommentId
import play.api.libs.json.{Json, Reads}

final case class CommentSave(
  message: String,
  parentId: Option[CommentId]
)

object CommentSave {
  implicit val jsonReads: Reads[CommentSave] = Json.reads[CommentSave]
}
