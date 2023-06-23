package model.db.dao.table

import model.PollId
import model.db.Poll
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class PollTable(tag: Tag) extends Table[Poll](tag, "poll") {
  val id = column[PollId]("id", O.AutoInc, O.PrimaryKey)
  val year = column[Int]("year")
  val question = column[String]("question")
  val isActive = column[Boolean]("is_active")
  val isDeleted = column[Boolean]("is_deleted")
  val created = column[DateTime]("timestamp")

  def * = (id, year, question, isActive, isDeleted, created) <>
    ((Poll.apply _).tupled, Poll.unapply)
}
