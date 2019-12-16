package util

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import javax.inject.{Inject, Singleton}
import model.api.{ChatMessage, ChatSave, PublicUserInfo}
import model.db
import model.db.dao.{ChatMessageDAO, ChatOnlineDAO}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class Chat @Inject() (
  chatMessageDAO: ChatMessageDAO,
  chatOnlineDAO: ChatOnlineDAO,
  displayNames: DisplayNames
)(implicit mat: Materializer) {
  val logger = Logger(getClass)

  private def post(userId: String, message: String): Future[db.ChatMessage] = {
    saveOnlineStatus(userId)

    chatMessageDAO.save(userId, message) map { chatMessage =>
      chatMessage
    }
  }

  private def saveOnlineStatus(userId: String): Future[Unit] = {
    chatOnlineDAO.save(userId) recover {
      case e =>
        logger.error("Error while saving chat online status.", e)
    }
  }


  // chat room many clients -> merge hub -> broadcasthub -> many clients
  private val (chatSink, chatSource) = {
    // Don't log MergeHub$ProducerFailed as error if the client disconnects.
    // recoverWithRetries -1 is essentially "recoverWith"
    val source = MergeHub.source[ChatMessage]
      .log("source")
      .recoverWithRetries(-1, { case _: Exception => Source.empty })

    val sink = BroadcastHub.sink[ChatMessage]
    source.toMat(sink)(Keep.both).run()
  }

  // source for online list
  private val onlineSource: Source[JsValue, _] = Source
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

  val lastMessagesSource: Source[Seq[ChatMessage], _] = chatSource
    .map{x =>
      println("a " + x)
      x
    }
    .scan(Seq.empty[ChatMessage]){
      case (previous, newMessage) => previous.takeRight(1) :+ newMessage
    }
    .map{x =>
      println("+ " + x)
      x
    }
    .expand(Iterator.continually(_))
    .map{x =>
      println("- " + x)
      x
    }

  private val lastMessagesJsonSource: Source[JsValue, _] = lastMessagesSource
    .take(1)
    .flatMapConcat(messages => { println(messages.map(_.message)); Source(messages.toList) })
    .map(Json.toJson[ChatMessage])

  private val chatJsonSource: Source[JsValue, _] = chatSource
    .map(Json.toJson[ChatMessage])

  def chatFlow(userId: String): Flow[JsValue, JsValue, _] = {
    val userSink = Flow[JsValue]
      .mapAsync(2){ body =>
        Json.fromJson[ChatSave](body).fold(
          errors => {
            Future.failed(throw new Exception("Invalid message"))
          },
          chatSave => {
            // TODO use actual user id and display name
            post(userId, chatSave.message)
              .map(dbChatMessage => ChatMessage.fromDb(dbChatMessage, "DisplayName"))
          }
        )
      }
      .to(chatSink)

    val userOnlineSource: Source[JsValue, _] = onlineSource.mapAsync(1) { message =>
      saveOnlineStatus(userId) map { _ =>
        message
      }
    }

    val userSource: Source[JsValue, _] = chatJsonSource merge userOnlineSource

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
      .via(Flow.fromSinkAndSource(
        userSink,
        initSource.concat(lastMessagesJsonSource).concat(userSource)
      ))
      .log("chatFlow")
  }
}
