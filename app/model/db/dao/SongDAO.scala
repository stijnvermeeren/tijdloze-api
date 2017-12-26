package model
package db
package dao

import javax.inject.{Inject, Singleton}

import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class SongDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(songId: SongId): Future[Song] = {
    db run {
      SongTable.filter(_.id === songId).result.head
    }
  }

  def getAll(): Future[Seq[Song]] = {
    db run {
      SongTable.result
    }
  }
}
