package model.db.dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import model.db.{LogUserDisplayName, LogUserDisplayNameId}
import org.joda.time.DateTime

private[table] trait LogUserDisplayNameTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class LogUserDisplayNameTable(tag: Tag) extends Table[LogUserDisplayName](tag, "log_user_display_name") {
    val id = column[LogUserDisplayNameId]("id", O.AutoInc, O.PrimaryKey)
    val userId = column[String]("user_id")
    val displayName = column[String]("display_name")
    val created = column[DateTime]("created")

    def * = (id, userId, displayName, created) <>
      ((LogUserDisplayName.apply _).tupled, LogUserDisplayName.unapply)
  }

  val LogUserDisplayNameTable = TableQuery[LogUserDisplayNameTable]
}
