package model
package api

import org.joda.time.DateTime
import play.api.libs.json.Json
import JsonWrites.dateTimeWriter

final case class ChatMessage(
  id: ChatMessageId = ChatMessageId(0),
  userId: String,
  message: String,
  timeStamp: DateTime = DateTime.now()
)

object ChatMessage {
  implicit val jsonWrites = Json.writes[ChatMessage]

  def fromDb(dbChatMessage: db.ChatMessage): ChatMessage = {
    ChatMessage(
      id = dbChatMessage.id,
      userId = dbChatMessage.userId,
      message = dbChatMessage.message,
      timeStamp = dbChatMessage.timeStamp
    )
  }
}
