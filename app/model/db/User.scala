package model.db

import org.joda.time.DateTime

final case class User(
  id: String,
  displayName: Option[String] = None,
  name: Option[String] = None,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  nickname: Option[String] = None,
  email: Option[String] = None,
  emailVerified: Boolean = false,
  created: DateTime = DateTime.now(),
  lastSeen: DateTime = DateTime.now(),
  isAdmin: Boolean = false
)
