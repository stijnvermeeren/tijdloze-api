package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.ListEntryTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ListEntryDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val listEntryTable = TableQuery[ListEntryTable]

  def getAll(): Future[Seq[ListEntry]] = {
    db run {
      listEntryTable.result
    }
  }

  def save(year: Int, position: Int, songId: SongId): Future[Unit] = {
    db run {
      val update = listEntryTable
        .filter(_.year === year)
        .filter(_.position === position)
        .map(_.songId)
        .update(songId)

      update flatMap { updateCount =>
        if (updateCount > 0) {
          DBIO.successful(())
        } else {
          listEntryTable += ListEntry(songId = songId, year = year, position = position)
        }
      }
    } map (_ => ())
  }

  def delete(year: Int, position: Int): Future[Int] = {
    db run {
      listEntryTable
        .filter(_.year === year)
        .filter(_.position === position)
        .delete
    }
  }

  def currentYear(): Future[Int] = {
    db run {
      listEntryTable.map(_.year).max.result
    } map (_.get)
  }

  def getByYear(year: Int): Future[Seq[ListEntry]] = {
    db run {
      listEntryTable.filter(_.year === year).result
    }
  }
}
