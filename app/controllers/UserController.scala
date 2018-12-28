package controllers

import javax.inject._
import model.api.{SetDisplayName, UserInfo, UserInfoAdmin, UserSave}
import model.db.dao.{LogUserDisplayNameDAO, UserDAO}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(
  cache: AsyncCacheApi,
  authenticate: Authenticate,
  authenticateAdmin: AuthenticateAdmin,
  optionallyAuthenticate: OptionallyAuthenticate,
  userDAO: UserDAO,
  logUserDisplayNameDAO: LogUserDisplayNameDAO
) extends InjectedController {

  def post() = (Action andThen optionallyAuthenticate).async(parse.json) { implicit request =>
    val data = request.body.validate[UserSave]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      userSave => {
        if (request.user.exists(_.isBlocked)) {
          Future.successful(Unauthorized("User is blocked."))
        } else {
          request.userId match {
            case Some(userId) =>
              userDAO.save(userId, userSave) flatMap { _ =>
                cache.remove("displayNames") flatMap { _ =>
                  userDAO.get(userId) map {
                    case Some(dbUser) =>
                      Ok(Json.toJson(UserInfo.fromDb(dbUser)))
                    case None =>
                      InternalServerError("Error while saving user info to the database.")
                  }
                }
              }
            case None =>
              Future.successful(Unauthorized("No userId found in JWT."))
          }
        }
      }
    )
  }

  def get() = (Action andThen authenticate).async { implicit request =>
    userDAO.get(request.user.id) map {
      case Some(dbUser) =>
        Ok(Json.toJson(UserInfo.fromDb(dbUser)))
      case None =>
        InternalServerError("Error while reading user info from the database.")
    }
  }

  def setDisplayName() = (Action andThen authenticate).async(parse.json)  { implicit request =>
    val data = request.body.validate[SetDisplayName]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      displayNameForm => {
        val displayName = displayNameForm.displayName
        if (displayName.nonEmpty) {
          userDAO.setDisplayName(request.user.id, displayNameForm.displayName) flatMap { _ =>
            logUserDisplayNameDAO.save(request.user.id, displayNameForm.displayName) flatMap { _ =>
              cache.remove("displayNames") flatMap { _ =>
                userDAO.get(request.user.id) map {
                  case Some(dbUser) =>
                    Ok(Json.toJson(UserInfo.fromDb(dbUser)))
                  case None =>
                    InternalServerError("Error while saving user info to the database.")
                }
              }
            }
          }
        } else {
          Future.successful(BadRequest("Display name must be non-empty."))
        }
      }
    )
  }

  def list() = {
    (Action andThen authenticateAdmin).async { implicit request =>
      userDAO.listAll() map { users =>
        Ok(Json.toJson(users map UserInfoAdmin.fromDb))
      }
    }
  }

  def block(userId: String) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      userDAO.setBlocked(userId, isBlocked = true) map { _ =>
        Ok("")
      }
    }
  }

  def unblock(userId: String) = {
    (Action andThen authenticateAdmin).async { implicit request =>
      userDAO.setBlocked(userId, isBlocked = false) map { _ =>
        Ok("")
      }
    }
  }
}
