package model
package db

import org.joda.time.DateTime

final case class Comment(
  id: CommentId = CommentId(0),
  parentId: Option[CommentId] = None,
  lastReply1Id: Option[CommentId] = None,
  lastReply2Id: Option[CommentId] = None,
  lastReply3Id: Option[CommentId] = None,
  name: Option[String] = None,
  userId: Option[String],
  versionId: Option[CommentVersionId] = None,
  timeStamp: DateTime = DateTime.now(),
  sortDate: DateTime = DateTime.now(),
  dateDeleted: Option[DateTime] = None
)
