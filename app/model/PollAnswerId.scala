package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class PollAnswerId(value: Int) extends MappedTo[Int]

object PollAnswerId {
  implicit val jsonWrites = new Writes[PollAnswerId] {
    def writes(pollAnswerId: PollAnswerId) = JsNumber(pollAnswerId.value)
  }

  implicit val pathBindable: PathBindable[PollAnswerId] = {
    PathBindable.bindableInt.transform(PollAnswerId.apply, _.value)
  }
}
