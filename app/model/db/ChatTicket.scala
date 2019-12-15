package model.db

import org.joda.time.DateTime

final case class ChatTicket(
  ticket: String,
  userId: String,
  created: DateTime = DateTime.now(),
  used: Option[DateTime] = None
)
