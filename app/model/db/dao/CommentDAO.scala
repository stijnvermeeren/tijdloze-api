package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CommentDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def save(userId: String, message: String): Future[Unit] = {
    db run {
      CommentTable += Comment(userId = Some(userId), message = message)
    } map (_ => ())
  }

  def delete(commentId: CommentId): Future[Unit] = {
    db run {
      CommentTable
        .filter(_.id === commentId)
        .map(_.dateDeleted)
        .update(Some(DateTime.now()))
    } map (_ => ())
  }

  def get(commentId: CommentId): Future[Option[Comment]] = {
    db run {
      CommentTable
        .filter(_.id === commentId)
        .result
        .headOption
    }
  }

  def count(): Future[Int] = {
    db run {
      CommentTable.length.result
    }
  }

  def listPage(page: Int): Future[Seq[(Comment, Option[User])]] = {
    val pageSize = 20
    db run {
      val joinedQuery = CommentTable.filter(_.dateDeleted.isEmpty) joinLeft UserTable on (_.userId === _.id)
      joinedQuery.sortBy(_._1.id.desc).drop(pageSize * (page - 1)).take(pageSize).result
    }
  }
}
