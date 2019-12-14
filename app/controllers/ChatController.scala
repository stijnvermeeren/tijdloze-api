package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import util.Chat
import javax.inject._
import model.api.{ChatMessage, ChatSave, PublicUserInfo}
import model.db.dao.ChatOnlineDAO
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class ChatController @Inject()(
  authenticate: Authenticate,
  optionallyAuthenticate: OptionallyAuthenticate,
  chat: Chat,
  chatOnlineDAO: ChatOnlineDAO
)(implicit mat: Materializer) extends InjectedController {

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
        println("init")
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

  // TODO secure with same origin check
  // TODO authenticate
  def ws(): WebSocket = {
    WebSocket.acceptOrResult[JsValue, JsValue] { requestHeader =>
      optionallyAuthenticate.auth(requestHeader) flatMap {
        case Right(user) =>
          chat.saveOnlineStatus(user.id) map { _ =>
            val flow = chatFlow(user.id)
            Right(flow)
          }
        case Left(e) =>
          Future.successful(Left(e))
      }
    }
  }
}
