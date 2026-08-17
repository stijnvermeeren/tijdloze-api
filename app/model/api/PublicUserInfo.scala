package model.api

import model.db
import play.api.libs.json.{Json, OWrites}

final case class PublicUserInfo(
  id: String,
  displayName: Option[String],
  isAdmin: Boolean,
  isBlocked: Boolean
)

object PublicUserInfo {
  implicit val jsonWrites: OWrites[PublicUserInfo] = Json.writes[PublicUserInfo]

  def fromDb(dbUser: db.User): PublicUserInfo = {
    PublicUserInfo(
      id = dbUser.id,
      displayName = dbUser.displayName,
      isAdmin = dbUser.isAdmin,
      isBlocked = dbUser.isBlocked
    )
  }
}
