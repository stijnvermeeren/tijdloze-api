package model
package db
package dao.table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

private[table] trait CrawlArtistTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class CrawlArtistTable(tag: Tag) extends Table[CrawlArtist](tag, "crawl_artist") {
    val id = column[CrawlArtistId]("id", O.AutoInc, O.PrimaryKey)
    val artistId = column[ArtistId]("artist_id")
    val crawlDate = column[DateTime]("crawl_date")
    val field = column[String]("field")
    val value = column[Option[String]]("value")
    val comment = column[Option[String]]("comment")
    val isAuto = column[Boolean]("is_auto")
    val isAccepted = column[Option[Boolean]]("is_accepted")

    def * = (id, artistId, crawlDate, field, value, comment, isAuto, isAccepted) <>
      ((CrawlArtist.apply _).tupled, CrawlArtist.unapply)
  }

  val CrawlArtistTable = TableQuery[CrawlArtistTable]
}
