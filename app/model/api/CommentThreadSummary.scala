package model.api

import play.api.libs.json.{Json, OWrites}

final case class CommentThreadSummary(
  mainComment: Comment,
  lastReply3: Option[Comment],
  lastReply2: Option[Comment],
  lastReply1: Option[Comment],
  replyCount: Int
)

object CommentThreadSummary {
  implicit val jsonWrites: OWrites[CommentThreadSummary] = Json.writes[CommentThreadSummary]
}
