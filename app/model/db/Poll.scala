package model
package db

import org.joda.time.DateTime

final case class Poll(
  id: PollId = PollId(0),
  year: Int,
  question: String,
  isActive: Boolean,
  isDeleted: Boolean,
  created: DateTime = DateTime.now()
)
