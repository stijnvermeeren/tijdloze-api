package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.{CommentTable, CommentVersionTable, UserTable}
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import model.api.{CommentThreadFull, CommentThreadSummary}
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

  def create(userId: String, message: String, parentId: Option[CommentId]): Future[Unit] = {
    for {
      commentId <- db run {
        (commentTable returning commentTable.map(_.id)) += Comment(userId = Some(userId), parentId = parentId)
      }
      _ <- update(commentId, message)
      _ <- parentId match {
        case Some(parentIdValue) => updateParent(parentIdValue)
        case _ => Future.successful(())
      }
    } yield ()
  }

  def updateParent(parentId: CommentId): Future[Unit] = {
    for {
      lastReplies <- db run {
        commentTable.filter(_.parentId === parentId).filter(_.dateDeleted.isEmpty).sortBy(_.id.desc).take(3).result
      }
      lastReply1 = lastReplies.lift(0)
      lastReply2 = lastReplies.lift(1)
      lastReply3 = lastReplies.lift(2)
      mainComment <- db run {
        commentTable.filter(_.id === parentId).result.headOption
      }
      sortDate = lastReply1.map(_.timeStamp).orElse(mainComment.map(_.timeStamp)).getOrElse(DateTime.now())
      _ <- db run {
        commentTable
          .filter(_.id === parentId)
          .map(comment => (comment.lastReply1Id, comment.lastReply2Id, comment.lastReply3Id, comment.sortDate))
          .update((lastReply1.map(_.id), lastReply2.map(_.id), lastReply3.map(_.id), sortDate))
      }
    } yield ()
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

  def fullThread(commentId: CommentId): Future[Option[CommentThreadFull]] = {
    val mainCommentFuture = db run {
      val joinedQuery = commentTable.filter(_.dateDeleted.isEmpty)
        .filter(_.id === commentId)
        .joinLeft(commentVersionTable).on(_.versionId === _.id)
        .joinLeft(userTable).on(_._1.userId === _.id)

      joinedQuery
        .map {
          case ((comment, version), user) => (comment, version, user)
        }
        .sortBy{
          case (comment, _, _) => comment.id.asc
        }
        .result.headOption
    }
    val repliesFuture = db run {
      val joinedQuery = commentTable.filter(_.dateDeleted.isEmpty)
        .filter(_.parentId === commentId)
        .joinLeft(commentVersionTable).on(_.versionId === _.id)
        .joinLeft(userTable).on(_._1.userId === _.id)

      joinedQuery
        .map {
          case ((comment, version), user) => (comment, version, user)
        }
        .sortBy{
          case (comment, _, _) => comment.id.asc
        }
        .result
    }

    for {
      mainComment <- mainCommentFuture
      replies <- repliesFuture
    } yield {
      mainComment map {
        case (comment, version, user) =>
          CommentThreadFull(
            api.Comment.fromDb(comment, version, user),
            replies map {
              case (replyComment, replyVersion, replyUser) =>
                api.Comment.fromDb(replyComment, replyVersion, replyUser)
            }
          )
      }
    }
  }

  def count(): Future[Int] = {
    db run {
      commentTable
        .filter(_.parentId.isEmpty)
        .filter(_.dateDeleted.isEmpty)
        .length.result
    }
  }

  def listPage(page: Int): Future[Seq[CommentThreadSummary]] = {
    val pageSize = 20
    db run {
      commentTable
        .filter(_.parentId.isEmpty)
        .filter(_.dateDeleted.isEmpty)
        .joinLeft(commentVersionTable).on(_.versionId === _.id)
        .joinLeft(userTable).on(_._1.userId === _.id)
        .map {
          case ((comment, version), user) =>
            val replyCount = commentTable.filter(_.parentId === comment.id).filter(_.dateDeleted.isEmpty).length
            ((comment, version, user), replyCount)
        }
        .joinLeft(commentTable).on(_._1._1.lastReply1Id === _.id)
        .joinLeft(commentVersionTable).on(_._2.flatMap(_.versionId) === _.id)
        .joinLeft(userTable).on(_._1._2.flatMap(_.userId) === _.id)
        .map{
          case ((((mainComment, replyCount), reply), replyVersion), replyUser) =>
            (mainComment, replyCount, (reply, replyVersion, replyUser))
        }
        .joinLeft(commentTable).on(_._1._1.lastReply2Id === _.id)
        .joinLeft(commentVersionTable).on(_._2.flatMap(_.versionId) === _.id)
        .joinLeft(userTable).on(_._1._2.flatMap(_.userId) === _.id)
        .map{
          case ((((mainComment, replyCount, reply1), reply), replyVersion), replyUser) =>
            (mainComment, replyCount, reply1, (reply, replyVersion, replyUser))
        }
        .joinLeft(commentTable).on(_._1._1.lastReply2Id === _.id)
        .joinLeft(commentVersionTable).on(_._2.flatMap(_.versionId) === _.id)
        .joinLeft(userTable).on(_._1._2.flatMap(_.userId) === _.id)
        .map{
          case ((((mainComment, replyCount, reply1, reply2), reply), replyVersion), replyUser) =>
            (mainComment, replyCount, reply1, reply2, (reply, replyVersion, replyUser))
        }
        .sortBy{
          case ((mainComment, _, _), _, _, _, _) => mainComment.sortDate.desc
        }
        .drop(pageSize * (page - 1))
        .take(pageSize)
        .result
    } map { comments =>
      comments map {
        case (mainComment, replyCount, reply1, reply2, reply3) =>
          def tupleToApiComment(tuple: (Option[Comment], Option[CommentVersion], Option[User])): Option[api.Comment] = {
            tuple match {
              case (Some(comment), version, user) =>
                Some(api.Comment.fromDb(comment, version, user))
              case _ =>
                None
            }
          }
          CommentThreadSummary(
            (api.Comment.fromDb _).tupled(mainComment),
            lastReply1 = tupleToApiComment(reply1),
            lastReply2 = tupleToApiComment(reply2),
            lastReply3 = tupleToApiComment(reply3),
            replyCount = replyCount
          )
      }
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
