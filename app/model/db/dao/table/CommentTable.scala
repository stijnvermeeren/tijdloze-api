package model
package db
package dao.table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class CommentTable(tag: Tag) extends Table[Comment](tag, "comment") {
  val id = column[CommentId]("id", O.AutoInc, O.PrimaryKey)
  val name = column[Option[String]]("naam")
  val userId = column[Option[String]]("user_id")
  val versionId = column[Option[CommentVersionId]]("version_id")
  val timestamp = column[DateTime]("tijdstip")
  val dateDeleted = column[Option[DateTime]]("date_deleted")

  def * = (id, name, userId, versionId, timestamp, dateDeleted) <>
    ((Comment.apply _).tupled, Comment.unapply)
}
