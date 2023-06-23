package model.db.dao.table

import com.github.tototoshi.slick.MySQLJodaSupport._
import model.CrawlField
import org.joda.time.DateTime
import slick.ast.TypedType
import slick.jdbc.MySQLProfile.api._

abstract class CrawlTable[
  Id : TypedType,
  Model,
  CrawlFieldT <: CrawlField[_, _, _] : TypedType
](tag: Tag, name: String) extends Table[Model](tag, name) {
  val id = column[Id]("id", O.AutoInc, O.PrimaryKey)
  val crawlDate = column[DateTime]("crawl_date")
  val field = column[CrawlFieldT]("field")
  val value = column[Option[String]]("value")
  val comment = column[Option[String]]("comment")
  val isAuto = column[Boolean]("is_auto")
  val isAccepted = column[Option[Boolean]]("is_accepted")
}
