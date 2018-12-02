package controllers

import javax.inject._
import model.api.{SetDisplayName, UserInfo, UserSave}
import model.db.dao.{LogUserDisplayNameDAO, UserDAO}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(authenticatedAction: AuthenticatedAction, userDAO: UserDAO, logUserDisplayNameDAO: LogUserDisplayNameDAO) extends InjectedController {
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

  def get() = authenticatedAction.async { implicit request =>
    userDAO.get(request.userId) map {
      case Some(dbUser) =>
        Ok(Json.toJson(UserInfo.fromDb(dbUser)))
      case None =>
        InternalServerError("Error while reading user info from the database.")
    }
  }

  def setDisplayName() = authenticatedAction.async(parse.json) { implicit request =>
    val data = request.body.validate[SetDisplayName]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      displayNameForm => {
        val displayName = displayNameForm.displayName
        if (displayName.nonEmpty) {
          userDAO.setDisplayName(request.userId, displayNameForm.displayName) flatMap { _ =>
            logUserDisplayNameDAO.save(request.userId, displayNameForm.displayName) flatMap { _ =>
              userDAO.get(request.userId) map {
                case Some(dbUser) =>
                  Ok(Json.toJson(UserInfo.fromDb(dbUser)))
                case None =>
                  InternalServerError("Error while saving user info to the database.")
              }
            }
          }
        } else {
          Future.successful(BadRequest("Display name must be non-empty."))
        }
      }
    )
  }
}
