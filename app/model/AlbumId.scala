package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class AlbumId(value: Int) extends MappedTo[Int]

object AlbumId {
  implicit val jsonWrites = new Writes[AlbumId] {
    def writes(albumId: AlbumId) = JsNumber(albumId.value)
  }

  implicit val pathBindable: PathBindable[AlbumId] = {
    PathBindable.bindableInt.transform(AlbumId.apply, _.value)
  }
}
