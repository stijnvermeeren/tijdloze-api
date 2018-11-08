package controllers

import javax.inject._
import model.api.{UserInfo, UserSave}
import model.db.dao.UserDAO
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(authenticatedAction: AuthenticatedAction, userDAO: UserDAO) extends InjectedController {
  def post() = authenticatedAction.async(parse.json) { implicit request =>
    val data = request.body.validate[UserSave]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      userSave => {
        userDAO.save(request.userId, userSave) flatMap { _ =>
          userDAO.get(request.userId) map {
            case Some(dbUser) =>
              Ok(Json.toJson(UserInfo.fromDb(dbUser)))
            case None =>
              InternalServerError("Error while saving user info to the database.")
          }
        }
      }
    )
  }
}
