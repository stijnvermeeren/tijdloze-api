package model.db.dao.table

import model.{CrawlSong, CrawlSongId, SongCrawlField, SongId}
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.jdbc.MySQLProfile.api._

class CrawlSongTable(tag: Tag) extends CrawlTable[CrawlSongId, CrawlSong, SongCrawlField](
  tag,
  name = "crawl_song"
) {
  val songId = column[SongId]("song_id")

  def * = (id, songId, crawlDate, field, value, comment, isAuto, isAccepted) <>
    ((CrawlSong.apply _).tupled, CrawlSong.unapply)
}
