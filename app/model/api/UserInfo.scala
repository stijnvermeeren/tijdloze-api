package model
package api

import play.api.libs.json.Json

final case class UserInfo(
  id: String,
  name: Option[String],
  email: Option[String],
  isAdmin: Boolean
)

object UserInfo {
  def fromDb(dbUser: db.User): UserInfo = {
    UserInfo(
      id = dbUser.id,
      name = dbUser.name,
      email = dbUser.email,
      isAdmin = dbUser.isAdmin
    )
  }

  implicit val jsonWrites = Json.writes[UserInfo]
}
