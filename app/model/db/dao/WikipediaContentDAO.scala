package model.db.dao

import model.db.WikipediaContent
import model.db.dao.table.WikipediaContentTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class WikipediaContentDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val wikipediaContentTable = TableQuery[WikipediaContentTable]

  def find(url: String): Future[Option[WikipediaContent]] = {
    db run {
      wikipediaContentTable.filter(_.url === url).result.headOption
    }
  }

  def insertOrUpdate(url: String, content: String): Future[Option[String]] = {
    val entry = WikipediaContent(
      url = url,
      content = content
    )

    db run {
      (wikipediaContentTable returning wikipediaContentTable.map(_.url)).insertOrUpdate(entry)
    }
  }

  def delete(url: String): Future[Int] = {
    db run {
      wikipediaContentTable.filter(_.url === url).delete
    }
  }
}
