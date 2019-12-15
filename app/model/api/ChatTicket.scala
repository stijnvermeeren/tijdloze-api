package model.api

import play.api.libs.json.Json

final case class ChatTicket(
  ticket: String
)

object ChatTicket {
  implicit val jsonWrites = Json.writes[ChatTicket]
}
