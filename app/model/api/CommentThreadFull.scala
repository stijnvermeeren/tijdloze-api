package model.api

import play.api.libs.json.{Json, OWrites}

final case class CommentThreadFull(
  mainComment: Comment,
  replies: Seq[Comment]
)

object CommentThreadFull {
  implicit val jsonWrites: OWrites[CommentThreadFull] = Json.writes[CommentThreadFull]
}
