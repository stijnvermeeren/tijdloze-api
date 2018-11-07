package model.db

import org.joda.time.DateTime

final case class User(
  id: String,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  nickName: Option[String] = None,
  email: Option[String] = None,
  created: DateTime = DateTime.now(),
  lastSeen: DateTime = DateTime.now(),
  isAdmin: Boolean = false
)
