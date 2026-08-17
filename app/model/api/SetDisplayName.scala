package model.api

import play.api.libs.json.{Json, Reads}

final case class SetDisplayName(
  displayName: String
)

object SetDisplayName {
  implicit val jsonReads: Reads[SetDisplayName] = Json.reads[SetDisplayName]
}

