package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class ArtistDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(artistId: ArtistId): Future[Artist] = {
    db run {
      ArtistTable.filter(_.id === artistId).result.head
    }
  }

  def getAll(): Future[Seq[Artist]] = {
    db run {
      ArtistTable.result
    }
  }
}
