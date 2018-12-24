package model.api

import play.api.libs.json.Json

final case class ChatSave(
  message: String
)

object ChatSave {
  implicit val jsonReads = Json.reads[ChatSave]
}
