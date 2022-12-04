package util.currentlist

import model.api.Poll
import play.api.libs.json.Json

final case class PollUpdate(
  poll: Poll
)

object PollUpdate {
  implicit val jsonWrites = Json.writes[PollUpdate]
}
