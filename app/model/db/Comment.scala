package model
package db

import org.joda.time.DateTime

final case class Comment(
  id: CommentId = CommentId(0),
  name: Option[String] = None,
  userId: Option[String],
  versionId: Option[CommentVersionId] = None,
  timeStamp: DateTime = DateTime.now(),
  dateDeleted: Option[DateTime] = None
)
