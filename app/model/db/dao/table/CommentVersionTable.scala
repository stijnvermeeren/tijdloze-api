package model
package db
package dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.jdbc.MySQLProfile.api._

class CommentVersionTable(tag: Tag) extends Table[CommentVersion](tag, "comment_version") {
  val id = column[CommentVersionId]("id", O.AutoInc, O.PrimaryKey)
  val commentId = column[CommentId]("comment_id")
  val message = column[String]("message")
  val created = column[DateTime]("created")

  def * = (id, commentId, message, created) <>
    ((CommentVersion.apply _).tupled, CommentVersion.unapply)
}
