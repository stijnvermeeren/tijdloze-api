package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.{CommentTable, CommentVersionTable, UserTable}
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CommentDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val commentTable = TableQuery[CommentTable]
  val commentVersionTable = TableQuery[CommentVersionTable]
  val userTable = TableQuery[UserTable]

  def create(userId: String, message: String): Future[Unit] = {
    for {
      commentId <- db run {
        (commentTable returning commentTable.map(_.id)) += Comment(userId = Some(userId))
      }
      _ <- update(commentId, message)
    } yield (())
  }

  def update(commentId: CommentId, message: String): Future[Unit] = {
    db run {
      for {
        versionId <- (commentVersionTable returning commentVersionTable.map(_.id)) += (
          CommentVersion(commentId = commentId, message = message)
        )
        _ <- commentTable.filter(_.id === commentId).map(_.versionId).update(Some(versionId))
      } yield (())
    }
  }

  def delete(commentId: CommentId): Future[Unit] = {
    db run {
      commentTable
        .filter(_.id === commentId)
        .map(_.dateDeleted)
        .update(Some(DateTime.now()))
    } map (_ => ())
  }

  def restore(commentId: CommentId): Future[Unit] = {
    db run {
      commentTable
        .filter(_.id === commentId)
        .map(_.dateDeleted)
        .update(None)
    } map (_ => ())
  }

  def get(commentId: CommentId): Future[Option[Comment]] = {
    db run {
      commentTable
        .filter(_.id === commentId)
        .result
        .headOption
    }
  }

  def count(): Future[Int] = {
    db run {
      commentTable.length.result
    }
  }

  def listPage(page: Int): Future[Seq[(Comment, Option[CommentVersion], Option[User])]] = {
    val pageSize = 20
    db run {
      val joinedQuery = commentTable.filter(_.dateDeleted.isEmpty)
        .joinLeft(commentVersionTable).on(_.versionId === _.id)
        .joinLeft(userTable).on(_._1.userId === _.id)

      joinedQuery
        .map {
          case ((comment, version), user) => (comment, version, user)
        }
        .sortBy{
          case (comment, _, _) => comment.id.desc
        }
        .drop(pageSize * (page - 1))
        .take(pageSize)
        .result
    }
  }

  def listDeleted(): Future[Seq[(Comment, Option[CommentVersion], Option[User])]] = {
    db run {
      val joinedQuery = commentTable.filter(_.dateDeleted.nonEmpty)
        .joinLeft(commentVersionTable).on(_.versionId === _.id)
        .joinLeft(userTable).on(_._1.userId === _.id)

      joinedQuery
        .map {
          case ((comment, version), user) => (comment, version, user)
        }
        .sortBy {
          case (comment, _, _) => comment.id.desc
        }
        .result
    }
  }
}
