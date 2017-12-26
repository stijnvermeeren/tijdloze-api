package model
package db
package dao

import javax.inject.{Inject, Singleton}

import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class AlbumDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(albumId: AlbumId): Future[Album] = {
    db run {
      AlbumTable.filter(_.id === albumId).result.head
    }
  }

  def getAll(): Future[Seq[Album]] = {
    db run {
      AlbumTable.result
    }
  }
}
