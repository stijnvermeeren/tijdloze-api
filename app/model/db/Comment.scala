package model
package db

import org.joda.time.DateTime

final case class Comment(
  id: CommentId,
  name: String,
  message: String,
  ip: String,
  timeStamp: DateTime
)
