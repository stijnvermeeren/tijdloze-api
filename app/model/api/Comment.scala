package model
package api

import org.joda.time.DateTime
import play.api.libs.json.Json
import JsonWrites.dateTimeWriter

final case class Comment(
  id: CommentId,
  name: String,
  message: String,
  created: DateTime
)

object Comment {
  implicit val jsonWrites = Json.writes[Comment]

  def fromDb(dbComment: db.Comment): Comment = {
    Comment(
      id = dbComment.id,
      name = dbComment.name,
      message = dbComment.message,
      created = dbComment.timeStamp
    )
  }
}
