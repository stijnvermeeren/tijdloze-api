package model
package db
package dao

import javax.inject.{Inject, Singleton}

import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class CommentDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def getAll(): Future[Seq[Comment]] = {
    db run {
      CommentTable.sortBy(_.id.asc).result
    }
  }
}
