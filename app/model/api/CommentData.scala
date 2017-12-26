package model
package api

import org.joda.time.DateTime
import play.api.libs.json.Json

import JsonWrites.dateTimeWriter

final case class CommentData(
  id: CommentId,
  created: DateTime
)

object CommentData {
  implicit val jsonWrites = Json.writes[CommentData]

  def fromDb(dbComment: db.Comment): CommentData = {
    CommentData(dbComment.id, dbComment.timeStamp)
  }
}
