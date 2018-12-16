package model
package db

final case class PollAnswer(
  id: PollAnswerId = PollAnswerId(0),
  pollId: PollId,
  answer: String,
  voteCount: Int = 0
)
