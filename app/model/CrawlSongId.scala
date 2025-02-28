package model

import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class CrawlSongId(value: Int)

object CrawlSongId {
  implicit val columnMapper: BaseColumnType[CrawlSongId] = MappedColumnType.base[CrawlSongId, Int](
    _.value,
    CrawlSongId.apply
  )

  implicit val jsonWrites = new Writes[CrawlSongId] {
    def writes(crawlSongId: CrawlSongId) = JsNumber(crawlSongId.value)
  }

  implicit val jsonReads = new Reads[CrawlSongId] {
    def reads(value: JsValue): JsResult[CrawlSongId] = {
      value.validate[Int].map(CrawlSongId.apply)
    }
  }

  implicit val pathBindable: PathBindable[CrawlSongId] = {
    PathBindable.bindableInt.transform(CrawlSongId.apply, _.value)
  }
}

