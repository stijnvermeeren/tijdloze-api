package controllers

import util.Chat
import javax.inject._
import model.api.ChatTicket
import model.db.dao.ChatTicketDAO
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChatController @Inject()(
  authenticate: Authenticate,
  chat: Chat,
  chatTicketDAO: ChatTicketDAO
) extends InjectedController {

  def ticket() = (Action andThen authenticate).async { implicit request =>
    chatTicketDAO.create(request.user.id) map { ticket =>
      Ok(Json.toJson(ChatTicket(ticket)))
    }
  }

  def ws(): WebSocket = {
    WebSocket.acceptOrResult[JsValue, JsValue] { requestHeader =>

      requestHeader.getQueryString("ticket") match {
        case Some(ticket) =>
          chatTicketDAO.use(ticket) map {
            case Some(userId) =>
              val flow = chat.chatFlow(userId)
              Right(flow)
            case None =>
              Left(Forbidden)
          }
        case None =>
          Future.successful(Left(Forbidden))
      }
    }
  }
}
