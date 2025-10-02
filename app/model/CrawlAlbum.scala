package model

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json.JodaWrites.JodaDateTimeWrites

final case class CrawlAlbum(
  id: CrawlAlbumId = CrawlAlbumId(0),
  albumId: AlbumId,
  crawlDate: DateTime,
  field: AlbumCrawlField,
  value: Option[String] = None,
  comment: Option[String] = None,
  isAuto: Boolean,
  isAccepted: Option[Boolean] = None,
)

object CrawlAlbum {
  implicit val jsonWrites = Json.writes[CrawlAlbum]
}
