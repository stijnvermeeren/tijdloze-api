package model
package db
package dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime

private[table] trait CommentVersionTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class CommentVersionTable(tag: Tag) extends Table[CommentVersion](tag, "comment_version") {
    val id = column[CommentVersionId]("id", O.AutoInc, O.PrimaryKey)
    val commentId = column[CommentId]("comment_id")
    val message = column[String]("message")
    val created = column[DateTime]("created")

    def * = (id, commentId, message, created) <>
      ((CommentVersion.apply _).tupled, CommentVersion.unapply)
  }

  val CommentVersionTable = TableQuery[CommentVersionTable]
}
