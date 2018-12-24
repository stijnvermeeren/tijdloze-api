package model.db

import org.joda.time.DateTime

final case class ChatOnline(
  userId: String,
  lastSeen: DateTime = DateTime.now()
)
