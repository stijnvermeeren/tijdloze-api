package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class SongId(value: Int)

object SongId {
  implicit val columnMapper: BaseColumnType[SongId] = MappedColumnType.base[SongId, Int](
    _.value,
    SongId.apply
  )

  implicit val jsonWrites = new Writes[SongId] {
    def writes(songId: SongId) = JsNumber(songId.value)
  }

  implicit val jsonReads = new Reads[SongId] {
    def reads(value: JsValue): JsResult[SongId] = {
      value.validate[Int].map(SongId.apply)
    }
  }

  implicit val pathBindable: PathBindable[SongId] = {
    PathBindable.bindableInt.transform(SongId.apply, _.value)
  }
}
