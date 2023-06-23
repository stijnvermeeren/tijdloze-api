package model.db.dao.table

import model.{PollAnswerId, PollId}
import model.db.PollAnswer
import slick.jdbc.MySQLProfile.api._

class PollAnswerTable(tag: Tag) extends Table[PollAnswer](tag, "poll_answer") {
  val id = column[PollAnswerId]("id", O.AutoInc, O.PrimaryKey)
  val pollId = column[PollId]("poll_id")
  val answer = column[String]("answer")
  val voteCount = column[Int]("vote_count")

  def * = (id, pollId, answer, voteCount) <>
    ((PollAnswer.apply _).tupled, PollAnswer.unapply)
}
