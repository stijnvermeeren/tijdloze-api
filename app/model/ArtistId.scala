package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class ArtistId(value: Int) extends MappedTo[Int]

object ArtistId {
  implicit val jsonWrites = new Writes[ArtistId] {
    def writes(artistId: ArtistId) = JsNumber(artistId.value)
  }

  implicit val jsonReads = new Reads[ArtistId] {
    def reads(value: JsValue): JsResult[ArtistId] = {
      value.validate[Int].map(ArtistId.apply)
    }
  }

  implicit val pathBindable: PathBindable[ArtistId] = {
    PathBindable.bindableInt.transform(ArtistId.apply, _.value)
  }
}

