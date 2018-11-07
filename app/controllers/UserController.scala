package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(authenticatedAction: AuthenticatedAction) extends InjectedController {
  def post() = authenticatedAction.async { implicit rs =>
    Future.successful(Ok(rs.userId.getOrElse("-NONE-")))
  }
}
