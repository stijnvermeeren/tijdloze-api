package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class PollId(value: Int)

object PollId {
  implicit val columnMapper: BaseColumnType[PollId] = MappedColumnType.base[PollId, Int](
    _.value,
    PollId.apply
  )

  implicit val jsonWrites = new Writes[PollId] {
    def writes(pollId: PollId) = JsNumber(pollId.value)
  }

  implicit val pathBindable: PathBindable[PollId] = {
    PathBindable.bindableInt.transform(PollId.apply, _.value)
  }
}
