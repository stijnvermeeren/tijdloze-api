package model

import play.api.libs.json.{JsNumber, Writes}
import slick.lifted.MappedTo

final case class PollAnswerId(value: Int) extends MappedTo[Int]

object PollAnswerId {
  implicit val jsonWrites = new Writes[PollAnswerId] {
    def writes(pollAnswerId: PollAnswerId) = JsNumber(pollAnswerId.value)
  }
}
