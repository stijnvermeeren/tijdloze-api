package controllers

import model.CommentId

import javax.inject._
import model.api.{Comment, CommentSave}
import model.db.dao.CommentDAO
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommentController @Inject()(
  authenticate: Authenticate,
  authenticateAdmin: AuthenticateAdmin,
  commentDAO: CommentDAO
)(implicit ec: ExecutionContext) extends InjectedController {
  def post() = (Action andThen authenticate).async(parse.json) { implicit request =>
    val data = request.body.validate[CommentSave]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      commentSave => {
        commentDAO.create(request.user.id, commentSave.message, commentSave.parentId) map { _ =>
          Ok("")
        }
      }
    )
  }

  def update(commentId: CommentId) = (Action andThen authenticate).async(parse.json) { implicit request =>
    val data = request.body.validate[CommentSave]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      commentSave => {
        commentDAO.get(commentId) flatMap { commentOption =>
          if (commentOption.exists(_.userId.contains(request.user.id))) {
            commentDAO.update(commentId, commentSave.message).map(_ => Ok(""))
          } else {
            Future.successful(Unauthorized("Not permitted to update3 comment."))
          }
        }
      }
    )
  }

  def delete(commentId: CommentId) = (Action andThen authenticate).async { implicit request =>
    commentDAO.get(commentId) flatMap { commentOption =>
      if (request.user.isAdmin || commentOption.exists(_.userId.contains(request.user.id))) {
        commentDAO.delete(commentId)
          .flatMap{ _ =>
            commentOption.flatMap(_.parentId) match {
              case Some(parentId) => commentDAO.updateParent(parentId)
              case None => Future.successful(())
            }
          }
          .map(_ => Ok(""))
      } else {
        Future.successful(Unauthorized("Not permitted to delete comment."))
      }
    }
  }

  def restore(commentId: CommentId) = (Action andThen authenticate).async { implicit request =>
    commentDAO.get(commentId) flatMap { commentOption =>
      if (request.user.isAdmin || commentOption.exists(_.userId.contains(request.user.id))) {
        commentDAO.restore(commentId).map(_ => Ok(""))
      } else {
        Future.successful(Unauthorized("Not permitted to restore comment."))
      }
    }
  }

  def fullComment(commentId: CommentId) = Action.async { implicit rs =>
    for {
      fullThread <- commentDAO.fullThread(commentId)
    } yield {
      Ok(Json.toJson(fullThread))
    }
  }

  def listPage(page: Int) = Action.async { implicit rs =>
    for {
      comments <- commentDAO.listPage(page)
    } yield {
      Ok(Json.toJson(comments))
    }
  }

  def listDeleted() = (Action andThen authenticateAdmin).async { implicit rs =>
    for {
      commentsWithUser <- commentDAO.listDeleted()
    } yield {
      Ok(Json.toJson(
        commentsWithUser map (Comment.fromDb _).tupled
      ))
    }
  }

  def count() = Action.async { implicit rs =>
    for {
      commentCount <- commentDAO.count()
    } yield {
      Ok(Json.toJson(
        Map("commentCount" -> commentCount)
      ))
    }
  }
}
