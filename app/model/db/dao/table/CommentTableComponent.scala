package model
package db
package dao.table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

private[table] trait CommentTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class CommentTable(tag: Tag) extends Table[Comment](tag, "reactie") {
    val id = column[CommentId]("id", O.AutoInc, O.PrimaryKey)
    val name = column[Option[String]]("naam")
    val userId = column[Option[String]]("user_id")
    val message = column[String]("bericht")
    val timestamp = column[DateTime]("tijdstip")
    val dateDeleted = column[Option[DateTime]]("date_deleted")

    def * = (id, name, userId, message, timestamp, dateDeleted) <>
      ((Comment.apply _).tupled, Comment.unapply)
  }

  val CommentTable = TableQuery[CommentTable]
}
