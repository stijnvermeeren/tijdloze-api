package controllers

import model.CommentId

import javax.inject._
import model.api.{Comment, CommentSave}
import model.db.dao.CommentDAO
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CommentController @Inject()(authenticate: Authenticate, commentDAO: CommentDAO) extends InjectedController {
  def post() = (Action andThen authenticate).async(parse.json) { implicit request =>
    val data = request.body.validate[CommentSave]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      commentSave => {
        commentDAO.create(request.user.id, commentSave.message) map { _ =>
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
      if (commentOption.exists(_.userId.contains(request.user.id))) {
        commentDAO.delete(commentId).map(_ => Ok(""))
      } else {
        Future.successful(Unauthorized("Not permitted to delete comment."))
      }
    }
  }

  def listPage(page: Int) = Action.async { implicit rs =>
    for {
      commentsWithUser <- commentDAO.listPage(page)
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
