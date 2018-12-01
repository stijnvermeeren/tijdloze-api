package model
package api

import org.joda.time.DateTime
import play.api.libs.json.Json
import JsonWrites.dateTimeWriter

final case class Comment(
  id: CommentId,
  name: String,
  userId: Option[String],
  message: String,
  created: DateTime
)

object Comment {
  implicit val jsonWrites = Json.writes[Comment]

  def fromDb(dbComment: db.Comment, user: Option[db.User]): Comment = {
    Comment(
      id = dbComment.id,
      name = user.flatMap(_.displayName) orElse dbComment.name getOrElse "",
      userId = user.map(_.id),
      message = dbComment.message,
      created = dbComment.timeStamp
    )
  }
}
