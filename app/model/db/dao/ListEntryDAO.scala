package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class ListEntryDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def getBySong(songId: SongId): Future[Seq[ListEntry]] = {
    db run {
      ListEntryTable.filter(_.songId === songId).result
    }
  }

  def getAll(): Future[Seq[ListEntry]] = {
    db run {
      ListEntryTable.result
    }
  }
}
