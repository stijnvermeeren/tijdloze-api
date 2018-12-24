package model

import play.api.libs.json.{JsNumber, Writes}
import slick.lifted.MappedTo

final case class ChatMessageId(value: Int) extends MappedTo[Int]

object ChatMessageId {
  implicit val jsonWrites = new Writes[ChatMessageId] {
    def writes(artistId: ChatMessageId) = JsNumber(artistId.value)
  }
}
