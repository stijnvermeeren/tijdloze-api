package controllers

import javax.inject._
import model.api.{Comment, CommentData}
import model.db.dao.CommentDAO
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CommentController @Inject()(commentDAO: CommentDAO) extends InjectedController {
  def listPage(page: Int) = Action.async { implicit rs =>
    for {
      commentsWithUser <- commentDAO.listPage(page)
    } yield {
      Ok(Json.toJson(
        commentsWithUser map (Comment.fromDb _).tupled
      ))
    }
  }

  def allData() = Action.async { implicit rs =>
    for {
      comments <- commentDAO.getAll()
    } yield {
      Ok(Json.toJson(
        comments map CommentData.fromDb
      ))
    }
  }
}
