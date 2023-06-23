package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.ListExitTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ListExitDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val listExitTable = TableQuery[ListExitTable]

  def save(year: Int, songId: SongId): Future[Unit] = {
    db run {
      listExitTable += ListExit(songId = songId, year = year)
    } map (_ => ())
  }

  def delete(year: Int, songId: SongId): Future[Int] = {
    db run {
      listExitTable
        .filter(_.year === year)
        .filter(_.songId === songId)
        .delete
    }
  }

  def deleteAll(year: Int): Future[Int] = {
    db run {
      listExitTable
        .filter(_.year === year)
        .delete
    }
  }

  def getByYear(year: Int): Future[Seq[ListExit]] = {
    db run {
      listExitTable.filter(_.year === year).result
    }
  }
}
