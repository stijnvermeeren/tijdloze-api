package model
package db

import org.joda.time.DateTime

final case class PollVote(
  id: PollVoteId = PollVoteId(0),
  userId: Option[String],
  pollId: PollId,
  answerId: PollAnswerId,
  created: DateTime = DateTime.now()
)
