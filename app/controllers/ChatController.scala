package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import util.Chat
import javax.inject._
import model.api.{ChatMessage, ChatSave, ChatTicket, PublicUserInfo}
import model.db.dao.{ChatOnlineDAO, ChatTicketDAO}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class ChatController @Inject()(
  authenticate: Authenticate,
  chat: Chat,
  chatOnlineDAO: ChatOnlineDAO,
  chatTicketDAO: ChatTicketDAO
)(implicit mat: Materializer) extends InjectedController {

  def ticket() = (Action andThen authenticate).async { implicit request =>
    chatTicketDAO.create(request.user.id) map { ticket =>
      Ok(Json.toJson(ChatTicket(ticket)))
    }
  }

  // chat room many clients -> merge hub -> broadcasthub -> many clients
  private val (chatSink, chatSource) = {
    // Don't log MergeHub$ProducerFailed as error if the client disconnects.
    // recoverWithRetries -1 is essentially "recoverWith"
    val source = MergeHub.source[JsValue]
      .log("source")
      .recoverWithRetries(-1, { case _: Exception => Source.empty })

    val sink = BroadcastHub.sink[JsValue]
    source.toMat(sink)(Keep.both).run()
  }

  // source for online list
  Source
    .tick(
      initialDelay = 15.seconds,
      interval = 15.seconds,
      tick = ()
    )
    .mapAsync(1) { _ =>
      chatOnlineDAO.list(maxAgeSeconds = 30) map { users =>
        Json.toJson(
          users map PublicUserInfo.fromDb
        )
      }
    }
    .to(chatSink)
    .run()

  private def chatFlow(userId: String): Flow[JsValue, JsValue, _] = {
    val userSink = Flow[JsValue]
      .mapAsync(2){ body =>
        Json.fromJson[ChatSave](body).fold(
          errors => {
            Future.failed(throw new Exception("Invalid message"))
          },
          chatSave => {
            // TODO use actual user id and display name
            chat
              .post(userId, chatSave.message)
              .map(dbChatMessage => ChatMessage.fromDb(dbChatMessage, "DisplayName"))
              .map(Json.toJson[ChatMessage])
          }
        )
      }
      .to(chatSink)

    val userSource = chatSource.mapAsync(1) { message =>
      // TODO don't save online status too often
      chat.saveOnlineStatus(userId) map { _ =>
        message
      }
    }

    // initialisation
    val initSource = Source
      .single(())
      .mapAsync(1) { _ =>
        chatOnlineDAO.list(maxAgeSeconds = 30) map { users =>
          Json.toJson(
            users map PublicUserInfo.fromDb
          )
        }
      }

    Flow[JsValue]
      .via(Flow.fromSinkAndSource(userSink, initSource.concat(userSource)))
      .log("chatFlow")
  }

  def ws(): WebSocket = {
    WebSocket.acceptOrResult[JsValue, JsValue] { requestHeader =>

      requestHeader.getQueryString("ticket") match {
        case Some(ticket) =>
          chatTicketDAO.use(ticket) map {
            case Some(userId) =>
              val flow = chatFlow(userId)
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
