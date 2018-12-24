package model
package db
package dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime

private[table] trait ChatMessageTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class ChatMessageTable(tag: Tag) extends Table[ChatMessage](tag, "chat_message") {
    val id = column[ChatMessageId]("id", O.AutoInc, O.PrimaryKey)
    val userId = column[String]("user_id")
    val message = column[String]("message")
    val created = column[DateTime]("created")

    def * = (id, userId, message, created) <>
      ((ChatMessage.apply _).tupled, ChatMessage.unapply)
  }

  val ChatMessageTable = TableQuery[ChatMessageTable]
}
