package util

import javax.inject.{Inject, Singleton}
import model.ChatMessageId
import model.db.ChatMessage
import model.db.dao.{ChatMessageDAO, ChatOnlineDAO}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class Chat @Inject() (chatMessageDAO: ChatMessageDAO, chatOnlineDAO: ChatOnlineDAO, displayNames: DisplayNames) {
  val logger = Logger(getClass)

  var messages: List[ChatMessage] = List.empty

  def post(userId: String, message: String): Future[ChatMessage] = {
    saveOnlineStatus(userId)

    chatMessageDAO.save(userId, message) map { chatMessage =>
      synchronized {
        messages = chatMessage :: messages
      }
      chatMessage
    }
  }

  def get(userId: String, sinceId: Int): Future[List[(ChatMessage, String)]] = {
    saveOnlineStatus(userId)

    displayNames.get() map { displayNames =>
      val responseMessages = if (sinceId > 0) {
        messages.takeWhile(_.id != ChatMessageId(sinceId))
      } else {
        messages.take(20)
      }
      responseMessages.reverse map { message =>
        (message, displayNames.getOrElse(message.userId, ""))
      }
    }
  }

  def saveOnlineStatus(userId: String): Future[Unit] = {
    chatOnlineDAO.save(userId) recover {
      case e =>
        logger.error("Error while saving chat online status.", e)
    }
  }
}
