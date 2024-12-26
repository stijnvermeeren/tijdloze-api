package controllers

import javax.inject._
import model.api.{Text, TextSave}
import model.db.dao.TextDAO
import play.api.libs.json.JsError
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TextController @Inject()(
  dataCache: DataCache,
  authenticateAdmin: AuthenticateAdmin,
  textDAO: TextDAO
)(implicit ec: ExecutionContext) extends InjectedController {

  def save(key: String) = {
    (Action andThen authenticateAdmin).async(parse.json) { implicit request =>
      val data = request.body.validate[TextSave]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        textSave => {
          textDAO.save(key, textSave.text) map { _ =>
            dataCache.TextCache.reload(key)
            Ok("")
          }
        }
      )
    }
  }

  def get(key: String) = {
    Action.async { implicit request =>
      dataCache.TextCache.load(key)
    }
  }
}
