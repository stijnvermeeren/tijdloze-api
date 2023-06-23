package model.db.dao.table

import model.db.PollVote
import model.{PollAnswerId, PollId, PollVoteId}
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class PollVoteTable(tag: Tag) extends Table[PollVote](tag, "poll_vote") {
  val id = column[PollVoteId]("id", O.AutoInc, O.PrimaryKey)
  val userId = column[Option[String]]("user_id")
  val pollId = column[PollId]("poll_id")
  val answerId = column[PollAnswerId]("answer_id")
  val created = column[DateTime]("timestamp")

  def * = (id, userId, pollId, answerId, created) <>
    ((PollVote.apply _).tupled, PollVote.unapply)
}
