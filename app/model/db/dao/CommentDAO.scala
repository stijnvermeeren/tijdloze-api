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

  def listPage(page: Int): Future[Seq[(Comment, Option[User])]] = {
    val pageSize = 20
    db run {
      val commentQuery = CommentTable.sortBy(_.id.desc).drop(pageSize * (page - 1)).take(pageSize)
      val joinedQuery = commentQuery joinLeft UserTable on (_.userId === _.id)
      joinedQuery.result
    }
  }
}
