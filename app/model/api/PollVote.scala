package model
package api

import play.api.libs.json.Json

final case class PollVote(
  pollId: PollId,
  answerId: PollAnswerId
)

object PollVote {
  def fromDb(dbVote: db.PollVote): PollVote = {
    PollVote(
      pollId = dbVote.pollId,
      answerId = dbVote.answerId
    )
  }

  implicit val jsonWrites = Json.writes[PollVote]
}

final case class PollVoteList(
  votes: Seq[PollVote]
)

object PollVoteList {
  implicit val jsonWrites = Json.writes[PollVoteList]
}
