package util.currentlist

import model.api.Poll
import play.api.libs.json.{Json, OWrites}

final case class PollUpdate(
  poll: Poll
)

object PollUpdate {
  implicit val jsonWrites: OWrites[PollUpdate] = Json.writes[PollUpdate]
}
