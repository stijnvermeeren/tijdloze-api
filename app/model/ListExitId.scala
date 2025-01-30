package model

import slick.jdbc.H2Profile.api._

final case class ListExitId(value: Int)

object ListExitId {
  implicit val columnMapper: BaseColumnType[ListExitId] = MappedColumnType.base[ListExitId, Int](
    _.value,
    ListExitId.apply
  )
}
