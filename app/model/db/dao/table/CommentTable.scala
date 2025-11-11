package model
package db
package dao.table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class CommentTable(tag: Tag) extends Table[Comment](tag, "comment") {
  val id = column[CommentId]("id", O.AutoInc, O.PrimaryKey)
  val parentId = column[Option[CommentId]]("parent_id")
  val lastReply1Id = column[Option[CommentId]]("last_reply_1_id")
  val lastReply2Id = column[Option[CommentId]]("last_reply_2_id")
  val lastReply3Id = column[Option[CommentId]]("last_reply_3_id")
  val name = column[Option[String]]("naam")
  val userId = column[Option[String]]("user_id")
  val versionId = column[Option[CommentVersionId]]("version_id")
  val timestamp = column[DateTime]("tijdstip")
  val sortDate = column[DateTime]("sort_date")
  val dateDeleted = column[Option[DateTime]]("date_deleted")

  def * = (id, parentId, lastReply1Id, lastReply2Id, lastReply3Id, name, userId, versionId, timestamp, sortDate, dateDeleted) <>
    ((Comment.apply _).tupled, Comment.unapply)
}
