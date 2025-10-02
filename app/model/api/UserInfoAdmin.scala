package model
package api

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json.JodaWrites.JodaDateTimeWrites

final case class UserInfoAdmin(
  id: String,
  displayName: Option[String],
  name: Option[String],
  lastSeen: DateTime,
  created: DateTime,
  isAdmin: Boolean,
  isBlocked: Boolean
)

object UserInfoAdmin {
  implicit val jsonWrites = Json.writes[UserInfoAdmin]

  def fromDb(dbUser: db.User): UserInfoAdmin = {
    UserInfoAdmin(
      id = dbUser.id,
      displayName = dbUser.displayName,
      name = dbUser.name,
      lastSeen = dbUser.lastSeen,
      created = dbUser.created,
      isAdmin = dbUser.isAdmin,
      isBlocked = dbUser.isBlocked
    )
  }
}
