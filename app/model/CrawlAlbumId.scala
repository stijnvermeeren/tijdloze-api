package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class CrawlAlbumId(value: Int) extends MappedTo[Int]

object CrawlAlbumId {
  implicit val jsonWrites = new Writes[CrawlAlbumId] {
    def writes(crawlArtistId: CrawlAlbumId) = JsNumber(crawlArtistId.value)
  }

  implicit val jsonReads = new Reads[CrawlAlbumId] {
    def reads(value: JsValue): JsResult[CrawlAlbumId] = {
      value.validate[Int].map(CrawlAlbumId.apply)
    }
  }

  implicit val pathBindable: PathBindable[CrawlAlbumId] = {
    PathBindable.bindableInt.transform(CrawlAlbumId.apply, _.value)
  }
}

