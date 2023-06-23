package model.db
package dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.jdbc.MySQLProfile.api._

class ChatTicketTable(tag: Tag) extends Table[ChatTicket](tag, "chat_ticket") {
  val ticket = column[String]("ticket", O.PrimaryKey)
  val userId = column[String]("user_id")
  val created = column[DateTime]("created")
  val used = column[Option[DateTime]]("used")

  def * = (ticket, userId, created, used) <>
    ((ChatTicket.apply _).tupled, ChatTicket.unapply)
}
