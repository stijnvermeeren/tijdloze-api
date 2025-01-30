package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class CrawlAlbumId(value: Int)

object CrawlAlbumId {
  implicit val columnMapper: BaseColumnType[CrawlAlbumId] = MappedColumnType.base[CrawlAlbumId, Int](
    _.value,
    CrawlAlbumId.apply
  )

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

