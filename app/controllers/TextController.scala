package controllers

import javax.inject._
import model.api.{Text, TextSave}
import model.db.dao.TextDAO
import play.api.cache.AsyncCacheApi
import play.api.libs.json.JsError
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TextController @Inject()(
  cache: AsyncCacheApi,
  authenticateAdmin: AuthenticateAdmin,
  textDAO: TextDAO
) extends InjectedController {

  def save(key: String) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[TextSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        textSave => {
          textDAO.save(key, textSave.text) map { _ =>
            Ok("")
          }
        }
      )
    }
  }

  def get(key: String) = {
    Action.async { implicit request =>
      textDAO.get(key) map {
        case Some(text) =>
          Ok(Json.toJson(Text.fromDb(text)))
        case None =>
          NotFound(s"No text with key $key found.")
      }
    }
  }
}
