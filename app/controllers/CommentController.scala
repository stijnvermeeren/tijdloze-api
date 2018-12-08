package controllers

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
        commentDAO.save(request.userId, commentSave.message) map { _ =>
          Ok("")
        }
      }
    )
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
