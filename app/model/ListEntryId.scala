package model

import slick.jdbc.H2Profile.api._

final case class ListEntryId(value: Int)

object ListEntryId {
  implicit val columnMapper: BaseColumnType[ListEntryId] = MappedColumnType.base[ListEntryId, Int](
    _.value,
    ListEntryId.apply
  )
}
