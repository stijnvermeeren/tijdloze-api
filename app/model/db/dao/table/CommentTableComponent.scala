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
    val ip = column[String]("ip")
    val timestamp = column[DateTime]("tijdstip")

    def * = (id, name, userId, message, ip, timestamp) <>
      ((Comment.apply _).tupled, Comment.unapply)
  }

  val CommentTable = TableQuery[CommentTable]
}
