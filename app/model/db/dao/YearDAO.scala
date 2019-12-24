package model.db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class YearDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def save(year: Int): Future[Unit] = {
    db run {
      YearTable += Year(year)
    } map (_ => ())
  }

  def delete(year: Int): Future[Unit] = {
    db run {
      YearTable
        .filter(_.year === year)
        .delete
    } map (_ => ())
  }

  def getAll(): Future[Seq[Int]] = {
    db run {
      YearTable.map(_.year).sortBy(_.asc).result
    }
  }

  def maxYear(): Future[Option[Int]] = {
    db run {
      YearTable.map(_.year).sortBy(_.desc).result.headOption
    }
  }
}
