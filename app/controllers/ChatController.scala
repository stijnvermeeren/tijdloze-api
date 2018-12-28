package controllers

import util.Chat
import javax.inject._
import model.api.{ChatMessage, ChatSave, PublicUserInfo}
import model.db.dao.ChatOnlineDAO
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChatController @Inject()(
  authenticate: Authenticate,
  chat: Chat,
  chatOnlineDAO: ChatOnlineDAO
) extends InjectedController {
  def post() = (Action andThen authenticate).async(parse.json) { request =>
    val data = request.body.validate[ChatSave]
    data.fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      chatSave => {
        chat.post(request.user.id, chatSave.message) map { _ =>
          Ok("")
        }
      }
    )
  }

  def online() = (Action andThen authenticate).async { request =>
    chatOnlineDAO.list(maxAgeSeconds = 30) map { users =>
      Ok(Json.toJson(
        users map PublicUserInfo.fromDb
      ))
    }
  }

  def get() = {
    (Action andThen authenticate).async { request =>
      val sinceId = request.getQueryString("since") map Integer.parseInt getOrElse 0
      chat.get(request.user.id, sinceId) map { messages =>
        Ok(Json.toJson(
          messages.map((ChatMessage.fromDb _).tupled)
        ))
      }
    }
  }
}
