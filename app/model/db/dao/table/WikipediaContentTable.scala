
package model.db.dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import model.db.WikipediaContent
import org.joda.time.DateTime
import slick.jdbc.MySQLProfile.api._

class WikipediaContentTable(tag: Tag) extends Table[WikipediaContent](tag, "wikipedia_content") {
  val url = column[String]("url", O.PrimaryKey)
  val content = column[String]("content")
  val lastUpdate = column[DateTime]("last_update")

  def * = (url, content, lastUpdate) <>
    ((WikipediaContent.apply _).tupled, WikipediaContent.unapply)
}
