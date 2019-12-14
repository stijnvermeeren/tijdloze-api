package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
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
      5.seconds, // delay of first tick
      5.seconds, // delay of subsequent ticks
      "tick" // element emitted each tick
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

    val userSource = chatSource.map { message =>
      // TODO don't save online status too often
      chat.saveOnlineStatus(userId)
      message
    }

    Flow[JsValue]
      .via(Flow.fromSinkAndSource(userSink, userSource))
      .log("chatFlow")
  }

  // TODO secure with same origin check
  // TODO authenticate
  def ws(): WebSocket = {
    WebSocket.acceptOrResult[JsValue, JsValue] { requestHeader =>
      optionallyAuthenticate.auth(requestHeader) map {
        _.map(user => chatFlow(user.id))
      }
    }
  }
}
