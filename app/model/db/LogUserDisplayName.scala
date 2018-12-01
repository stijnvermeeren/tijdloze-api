package model.db

import org.joda.time.DateTime
import slick.lifted.MappedTo

final case class LogUserDisplayName(
  id: LogUserDisplayNameId = LogUserDisplayNameId(0),
  userId: String,
  displayName: String,
  created: DateTime = DateTime.now()
)

final case class LogUserDisplayNameId(value: Int) extends MappedTo[Int]
