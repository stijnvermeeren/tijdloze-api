package model.db

import org.joda.time.DateTime
import slick.jdbc.H2Profile.api._

final case class LogUserDisplayName(
  id: LogUserDisplayNameId = LogUserDisplayNameId(0),
  userId: String,
  displayName: String,
  created: DateTime = DateTime.now()
)

final case class LogUserDisplayNameId(value: Int)

object LogUserDisplayNameId {
  implicit val columnMapper: BaseColumnType[LogUserDisplayNameId] = MappedColumnType.base[LogUserDisplayNameId, Int](
    _.value,
    LogUserDisplayNameId.apply
  )
}
