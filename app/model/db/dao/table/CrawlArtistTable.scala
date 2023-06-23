package model
package db
package dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class CrawlArtistTable(tag: Tag) extends CrawlTable[CrawlArtistId, CrawlArtist, ArtistCrawlField](
  tag,
  name = "crawl_artist"
) {
  val artistId = column[ArtistId]("artist_id")

  def * = (id, artistId, crawlDate, field, value, comment, isAuto, isAccepted) <>
    ((CrawlArtist.apply _).tupled, CrawlArtist.unapply)
}
