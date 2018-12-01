package model.api

import play.api.libs.json.Json

final case class SetDisplayName(
  displayName: String
)

object SetDisplayName {
  implicit val jsonReads = Json.reads[SetDisplayName]
}

