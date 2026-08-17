package model.api

import play.api.libs.json.{Json, OWrites}

final case class ChatTicket(
  ticket: String
)

object ChatTicket {
  implicit val jsonWrites: OWrites[ChatTicket] = Json.writes[ChatTicket]
}
