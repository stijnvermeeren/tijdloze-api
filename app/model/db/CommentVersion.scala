package model
package db

import org.joda.time.DateTime

final case class CommentVersion(
  id: CommentVersionId = CommentVersionId(0),
  commentId: CommentId,
  message: String,
  created: DateTime = DateTime.now()
)
