package model
package db
package dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime

private[table] trait ChatOnlineTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class ChatOnlineTable(tag: Tag) extends Table[ChatOnline](tag, "chat_online") {
    val userId = column[String]("user_id", O.PrimaryKey)
    val lastSeen = column[DateTime]("last_seen")

    def * = (userId, lastSeen) <>
      ((ChatOnline.apply _).tupled, ChatOnline.unapply)
  }

  val ChatOnlineTable = TableQuery[ChatOnlineTable]
}
