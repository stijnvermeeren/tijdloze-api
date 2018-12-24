package util

import javax.inject.{Inject, Singleton}
import model.ChatMessageId
import model.db.ChatMessage
import model.db.dao.{ChatMessageDAO, ChatOnlineDAO}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class Chat @Inject() (chatMessageDAO: ChatMessageDAO, chatOnlineDAO: ChatOnlineDAO) {
  val logger = Logger(getClass)

  var messages: List[ChatMessage] = List.empty

  def post(userId: String, message: String): Future[Unit] = {
    saveOnlineStatus(userId)

    chatMessageDAO.save(userId, message) map { chatMessage =>
      synchronized {
        messages = chatMessage :: messages
      }
    }
  }

  def get(userId: String, sinceId: Int): List[ChatMessage] = {
    saveOnlineStatus(userId)

    messages.takeWhile(_.id != ChatMessageId(sinceId)).reverse
  }

  def saveOnlineStatus(userId: String): Unit = {
    chatOnlineDAO.save(userId).failed foreach { e =>
      logger.error("Error while saving chat online status.", e)
    }
  }
}
