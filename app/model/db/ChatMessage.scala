package model
package db

import org.joda.time.DateTime

final case class ChatMessage(
  id: ChatMessageId = ChatMessageId(0),
  userId: String,
  message: String,
  timeStamp: DateTime = DateTime.now()
)
