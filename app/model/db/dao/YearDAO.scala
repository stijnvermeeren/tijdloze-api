package model.db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.YearTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class YearDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val yearTable = TableQuery[YearTable]

  def save(year: Int): Future[Unit] = {
    db run {
      yearTable += Year(year)
    } map (_ => ())
  }

  def delete(year: Int): Future[Unit] = {
    db run {
      yearTable
        .filter(_.year === year)
        .delete
    } map (_ => ())
  }

  def getAll(): Future[Seq[Int]] = {
    db run {
      yearTable.map(_.year).sortBy(_.asc).result
    }
  }

  def maxYear(): Future[Option[Int]] = {
    db run {
      yearTable.map(_.year).sortBy(_.desc).result.headOption
    }
  }
}
