package model
package api

import play.api.libs.json.Json

final case class UserInfo(
  id: String,
  displayName: Option[String],
  name: Option[String],
  email: Option[String],
  isAdmin: Boolean,
  isBlocked: Boolean
)

object UserInfo {
  def fromDb(dbUser: db.User): UserInfo = {
    UserInfo(
      id = dbUser.id,
      displayName = dbUser.displayName,
      name = dbUser.name,
      email = dbUser.email,
      isAdmin = dbUser.isAdmin,
      isBlocked = dbUser.isBlocked
    )
  }

  implicit val jsonWrites = Json.writes[UserInfo]
}
