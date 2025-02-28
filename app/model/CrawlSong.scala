package model

import org.joda.time.DateTime
import play.api.libs.json.Json
import JsonWrites.dateTimeWriter

final case class CrawlSong(
  id: CrawlSongId = CrawlSongId(0),
  songId: SongId,
  crawlDate: DateTime,
  field: SongCrawlField,
  value: Option[String] = None,
  comment: Option[String] = None,
  isAuto: Boolean,
  isAccepted: Option[Boolean] = None,
)

object CrawlSong {
  implicit val jsonWrites = Json.writes[CrawlSong]
}
