package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class AlbumId(value: Int) extends MappedTo[Int]

object AlbumId {
  implicit val jsonWrites = new Writes[AlbumId] {
    def writes(albumId: AlbumId) = JsNumber(albumId.value)
  }

  implicit val jsonReads = new Reads[AlbumId] {
    def reads(value: JsValue): JsResult[AlbumId] = {
      value.validate[Int].map(AlbumId.apply)
    }
  }

  implicit val pathBindable: PathBindable[AlbumId] = {
    PathBindable.bindableInt.transform(AlbumId.apply, _.value)
  }
}
