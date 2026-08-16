package model
package api

import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}
import play.api.libs.json.JodaWrites.JodaDateTimeWrites

final case class ChatMessage(
  id: ChatMessageId = ChatMessageId(0),
  userId: String,
  displayName: String,
  message: String,
  created: DateTime = DateTime.now()
)

object ChatMessage {
  implicit val jsonWrites: OWrites[ChatMessage] = Json.writes[ChatMessage]

  def fromDb(dbChatMessage: db.ChatMessage, displayName: String): ChatMessage = {
    ChatMessage(
      id = dbChatMessage.id,
      userId = dbChatMessage.userId,
      displayName = displayName,
      message = dbChatMessage.message,
      created = dbChatMessage.created
    )
  }
}
