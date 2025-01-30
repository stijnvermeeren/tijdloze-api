package model

import slick.jdbc.H2Profile.api._

final case class PollVoteId(value: Int)

object PollVoteId {
  implicit val columnMapper: BaseColumnType[PollVoteId] = MappedColumnType.base[PollVoteId, Int](
    _.value,
    PollVoteId.apply
  )
}
