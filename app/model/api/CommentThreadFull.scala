package model.api

import play.api.libs.json.Json

final case class CommentThreadFull(
  mainComment: Comment,
  replies: Seq[Comment]
)

object CommentThreadFull {
  implicit val jsonWrites = Json.writes[CommentThreadFull]
}
