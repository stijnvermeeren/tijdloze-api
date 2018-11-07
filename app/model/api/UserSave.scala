package model.api

import play.api.libs.json.Json

final case class UserSave(
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String]
)

object UserSave {
  implicit val jsonWrites = Json.writes[Song]
}


