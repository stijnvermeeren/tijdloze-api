package model
package db.dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class CrawlAlbumTable(tag: Tag) extends CrawlTable[CrawlAlbumId, CrawlAlbum, AlbumCrawlField](
  tag,
  name = "crawl_album"
) {
  val albumId = column[AlbumId]("album_id")

  def * = (id, albumId, crawlDate, field, value, comment, isAuto, isAccepted) <>
    ((CrawlAlbum.apply _).tupled, CrawlAlbum.unapply)
}
