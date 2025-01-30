package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class CrawlArtistId(value: Int)

object CrawlArtistId {
  implicit val columnMapper: BaseColumnType[CrawlArtistId] = MappedColumnType.base[CrawlArtistId, Int](
    _.value,
    CrawlArtistId.apply
  )

  implicit val jsonWrites = new Writes[CrawlArtistId] {
    def writes(crawlArtistId: CrawlArtistId) = JsNumber(crawlArtistId.value)
  }

  implicit val jsonReads = new Reads[CrawlArtistId] {
    def reads(value: JsValue): JsResult[CrawlArtistId] = {
      value.validate[Int].map(CrawlArtistId.apply)
    }
  }

  implicit val pathBindable: PathBindable[CrawlArtistId] = {
    PathBindable.bindableInt.transform(CrawlArtistId.apply, _.value)
  }
}

