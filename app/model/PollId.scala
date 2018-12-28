package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class PollId(value: Int) extends MappedTo[Int]

object PollId {
  implicit val jsonWrites = new Writes[PollId] {
    def writes(pollId: PollId) = JsNumber(pollId.value)
  }

  implicit val pathBindable: PathBindable[PollId] = {
    PathBindable.bindableInt.transform(PollId.apply, _.value)
  }
}
