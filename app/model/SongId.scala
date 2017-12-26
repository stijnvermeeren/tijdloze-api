package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class SongId(value: Int) extends MappedTo[Int]

object SongId {
  implicit val jsonWrites = new Writes[SongId] {
    def writes(songId: SongId) = JsNumber(songId.value)
  }

  implicit val pathBindable: PathBindable[SongId] = {
    PathBindable.bindableInt.transform(SongId.apply, _.value)
  }
}