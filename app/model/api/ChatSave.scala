package model.api

import play.api.libs.json.{Json, Reads}

final case class ChatSave(
  message: String
)

object ChatSave {
  implicit val jsonReads: Reads[ChatSave] = Json.reads[ChatSave]
}
