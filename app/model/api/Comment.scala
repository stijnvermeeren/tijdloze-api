package model
package api

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json.JodaWrites.JodaDateTimeWrites

final case class Comment(
  id: CommentId,
  name: String,
  userId: Option[String],
  isAdmin: Option[Boolean],
  message: String,
  created: DateTime,
  updated: DateTime,
  deleted: Option[DateTime]
)

object Comment {
  implicit val jsonWrites = Json.writes[Comment]

  def fromDb(dbComment: db.Comment, version: Option[db.CommentVersion], user: Option[db.User]): Comment = {
    Comment(
      id = dbComment.id,
      name = user.flatMap(_.displayName) orElse dbComment.name getOrElse "",
      userId = user.map(_.id),
      isAdmin = user.map(_.isAdmin).filter(identity),
      message = version.map(_.message).getOrElse(""),
      created = dbComment.timeStamp,
      updated = version.map(_.created).getOrElse(dbComment.timeStamp),
      deleted = dbComment.dateDeleted
    )
  }
}
