package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ListExitDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def save(year: Int, songId: SongId): Future[Unit] = {
    db run {
      ListExitTable += ListExit(songId = songId, year = year)
    } map (_ => ())
  }

  def delete(year: Int, songId: SongId): Future[Int] = {
    db run {
      ListExitTable
        .filter(_.year === year)
        .filter(_.songId === songId)
        .delete
    }
  }

  def deleteAll(year: Int): Future[Int] = {
    db run {
      ListExitTable
        .filter(_.year === year)
        .delete
    }
  }

  def getByYear(year: Int): Future[Seq[ListExit]] = {
    db run {
      ListExitTable.filter(_.year === year).result
    }
  }
}
