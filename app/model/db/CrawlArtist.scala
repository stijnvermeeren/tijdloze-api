package model
package db

import org.joda.time.DateTime

final case class CrawlArtist(
  id: CrawlArtistId = CrawlArtistId(0),
  artistId: ArtistId,
  crawlDate: DateTime,
  field: String,
  value: Option[String] = None,
  comment: Option[String] = None,
  isAuto: Boolean,
  isAccepted: Option[Boolean] = None,
)
