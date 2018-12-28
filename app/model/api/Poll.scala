package model
package api

import play.api.libs.json.Json

final case class PollAnswer(
  id: PollAnswerId,
  answer: String,
  voteCount: Int
)

object PollAnswer {
  def fromDb(dbAnswer: db.PollAnswer): PollAnswer = {
    PollAnswer(
      id = dbAnswer.id,
      answer = dbAnswer.answer,
      voteCount = dbAnswer.voteCount
    )
  }

  implicit val jsonWrites = Json.writes[PollAnswer]
}

final case class Poll(
  id: PollId,
  question: String,
  year: Int,
  answers: Seq[PollAnswer],
  isActive: Boolean = true,
  isDeleted: Boolean = false
)

object Poll {
  def fromDb(dbPoll: db.Poll, dbAnswers: Seq[db.PollAnswer]): Poll = {
    Poll(
      id = dbPoll.id,
      question = dbPoll.question,
      year = dbPoll.year,
      answers = dbAnswers.map(PollAnswer.fromDb),
      isActive = dbPoll.isActive,
      isDeleted = dbPoll.isDeleted
    )
  }

  implicit val jsonWrites = Json.writes[Poll]
}
