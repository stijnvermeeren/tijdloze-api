package util

import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import javax.inject.{Inject, Singleton}
import model.api.{ChatMessage, ChatSave, PublicUserInfo}
import model.db
import model.db.dao.{ChatMessageDAO, ChatOnlineDAO, UserDAO}
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class Chat @Inject() (
  chatMessageDAO: ChatMessageDAO,
  chatOnlineDAO: ChatOnlineDAO,
  userDAO: UserDAO
)(implicit mat: Materializer) {
  val logger = Logger(getClass)
  val lastMessagesSize: Int = 10

  def post(userId: String, message: String): Future[ChatMessage] = {
    saveOnlineStatus(userId)

    userDAO.get(userId) flatMap {
      case Some(user) if !user.isBlocked =>
        chatMessageDAO.save(userId, message) map { dbChatMessage =>
          // TODO: how to properly deal with nullable displayName in DB?
          ChatMessage.fromDb(dbChatMessage, user.displayName.getOrElse(""))
        }
      case Some(user) if user.isBlocked =>
        Future.failed(new Exception("User is blocked"))
      case None =>
        Future.failed(new Exception("User not found"))
    }

  }

  def saveOnlineStatus(userId: String): Future[Unit] = {
    chatOnlineDAO.save(userId) recover {
      case e =>
        logger.error("Error while saving chat online status.", e)
    }
  }

  private def loadOnlineList(): Future[JsValue] = {
    chatOnlineDAO.list(maxAgeSeconds = 30) map { users =>
      Json.toJson(
        users map PublicUserInfo.fromDb
      )
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

  private val onlineTicksSource = Source.tick(
    initialDelay = 15.seconds,
    interval = 15.seconds,
    tick = ()
  )

  private val onlineActorSourceDef = Source.actorRef(bufferSize = 0, OverflowStrategy.dropNew)

  // TODO: implement this without needing a BroadcastHub?
  private val (onlineActorSourceMat, onlineActorSource) = onlineActorSourceDef.toMat(BroadcastHub.sink[JsValue])(Keep.both).run()

  // source for online list
  private val onlineSource: Source[JsValue, _] = {
    val mergedSource = (onlineTicksSource merge onlineActorSource).mapAsync(1) { _ =>
      loadOnlineList()
    }
    // allow individual users to connect dynamically / avoid re-materializing this stream for every user
    mergedSource.toMat(BroadcastHub.sink)(Keep.right).run()
  }

  private val lastMessagesSource: Source[Seq[ChatMessage], _] = {
    chatSource
      .scan(Seq.empty[ChatMessage]){
        case (previous, newMessage) => previous.takeRight(lastMessagesSize - 1) :+ newMessage
      }
      .expand(Iterator.continually(_))
      // Ensure the lastMessageSource does not backpressure the chat stream.
      // Need to throttle this to avoid draining the CPU.
      // This is a hack and won't work well if we have a bigger throughput.
      .throttle(elements = 1000, per = 1.second)
      .toMat(BroadcastHub.sink)(Keep.right).run() // allow individual users to connect dynamically
  }

  // Keep draining the lastMessagesSource so that it never backpressures.
  lastMessagesSource.runWith(Sink.ignore)

  def chatFlow(userId: String): Flow[JsValue, JsValue, _] = {
    val userSink = Flow[JsValue]
      .mapAsync(2){ body =>
        Json.fromJson[ChatSave](body).fold(
          errors => {
            Future.failed(throw new Exception("Invalid message"))
          },
          chatSave => {
            post(userId, chatSave.message)
          }
        )
      }
      .to(chatSink)

    val userOnlineSource: Source[JsValue, _] = onlineSource.mapAsync(1) { message =>
      saveOnlineStatus(userId) map { _ =>
        message
      }
    }

    val chatJsonSource: Source[JsValue, _] = chatSource.map(Json.toJson[ChatMessage])
    val userSource: Source[JsValue, _] = chatJsonSource merge userOnlineSource

    val userLastMessagesJsonSource = lastMessagesSource
      .take(1)
      .mapConcat(_.toList)
      .map(Json.toJson[ChatMessage])

    // initialisation
    val initSource = Source
      .single(())
      .mapAsync(1) { _ =>
        saveOnlineStatus(userId) flatMap { _ =>
          // Immediately send new online list to all other chat users
          val unit = () // avoid compiler warning
          onlineActorSourceMat ! unit

          // Ensure the current user has the up-to-date online list as well, even if he misses the previous message.
          loadOnlineList()
        }

      }

    Flow[JsValue]
      .via(Flow.fromSinkAndSource(
        userSink,
        initSource.concat(userLastMessagesJsonSource).concat(userSource)
      ))
      .log("chatFlow")
  }
}
