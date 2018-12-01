package model
package db

import org.joda.time.DateTime

final case class Comment(
  id: CommentId,
  name: Option[String],
  userId: Option[String],
  message: String,
  ip: String,
  timeStamp: DateTime
)
