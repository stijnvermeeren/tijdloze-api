package model
package api

import org.joda.time.DateTime

final case class Comment(
  id: CommentId,
  name: String,
  message: String,
  created: DateTime
)
