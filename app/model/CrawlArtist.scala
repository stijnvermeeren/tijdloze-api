package model

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json.JodaWrites.JodaDateTimeWrites

final case class CrawlArtist(
  id: CrawlArtistId = CrawlArtistId(0),
  artistId: ArtistId,
  crawlDate: DateTime,
  field: ArtistCrawlField,
  value: Option[String] = None,
  comment: Option[String] = None,
  isAuto: Boolean,
  isAccepted: Option[Boolean] = None,
)

object CrawlArtist {
  implicit val jsonWrites = Json.writes[CrawlArtist]
}
