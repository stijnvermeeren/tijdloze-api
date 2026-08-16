package model.api

import play.api.libs.json.{Json, Reads}

final case class UserSave(
  name: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  nickname: Option[String],
  email: Option[String],
  emailVerified: Option[Boolean]
)

object UserSave {
  implicit val jsonReads: Reads[UserSave] = Json.reads[UserSave]
}


