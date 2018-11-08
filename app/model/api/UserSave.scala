package model.api

import play.api.libs.json.Json

final case class UserSave(
  name: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  nickname: Option[String],
  email: Option[String],
  emailVerified: Option[Boolean]
)

object UserSave {
  implicit val jsonReads = Json.reads[UserSave]
}


