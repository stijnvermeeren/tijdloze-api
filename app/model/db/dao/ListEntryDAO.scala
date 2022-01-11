package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ListEntryDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def getAll(): Future[Seq[ListEntry]] = {
    db run {
      ListEntryTable.result
    }
  }

  def save(year: Int, position: Int, songId: SongId): Future[Unit] = {
    db run {
      val update = ListEntryTable
        .filter(_.year === year)
        .filter(_.position === position)
        .map(_.songId)
        .update(songId)

      update flatMap { updateCount =>
        if (updateCount > 0) {
          DBIO.successful(())
        } else {
          ListEntryTable += ListEntry(songId = songId, year = year, position = position)
        }
      }
    } map (_ => ())
  }

  def delete(year: Int, position: Int): Future[Int] = {
    db run {
      ListEntryTable
        .filter(_.year === year)
        .filter(_.position === position)
        .delete
    }
  }

  def currentYear(): Future[Int] = {
    db run {
      ListEntryTable.map(_.year).max.result
    } map (_.get)
  }

  def getByYear(year: Int): Future[Seq[ListEntry]] = {
    db run {
      ListEntryTable.filter(_.year === year).result
    }
  }
}
