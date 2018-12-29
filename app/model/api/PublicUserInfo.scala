package model.api

import model.db
import play.api.libs.json.Json

final case class PublicUserInfo(
  id: String,
  displayName: Option[String],
  isAdmin: Boolean
)

object PublicUserInfo {
  implicit val jsonWrites = Json.writes[PublicUserInfo]

  def fromDb(dbUser: db.User): PublicUserInfo = {
    PublicUserInfo(
      id = dbUser.id,
      displayName = dbUser.displayName,
      isAdmin = dbUser.isAdmin
    )
  }
}
