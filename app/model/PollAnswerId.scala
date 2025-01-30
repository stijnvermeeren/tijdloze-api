package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class PollAnswerId(value: Int)

object PollAnswerId {
  implicit val columnMapper: BaseColumnType[PollAnswerId] = MappedColumnType.base[PollAnswerId, Int](
    _.value,
    PollAnswerId.apply
  )

  implicit val jsonWrites = new Writes[PollAnswerId] {
    def writes(pollAnswerId: PollAnswerId) = JsNumber(pollAnswerId.value)
  }

  implicit val pathBindable: PathBindable[PollAnswerId] = {
    PathBindable.bindableInt.transform(PollAnswerId.apply, _.value)
  }
}
