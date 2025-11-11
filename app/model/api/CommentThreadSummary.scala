package model.api

import play.api.libs.json.Json

final case class CommentThreadSummary(
  mainComment: Comment,
  lastReply3: Option[Comment],
  lastReply2: Option[Comment],
  lastReply1: Option[Comment],
  replyCount: Int
)

object CommentThreadSummary {
  implicit val jsonWrites = Json.writes[CommentThreadSummary]
}
