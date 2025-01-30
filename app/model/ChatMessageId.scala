package model

import play.api.libs.json.{JsNumber, Writes}
import slick.jdbc.H2Profile.api._

final case class ChatMessageId(value: Int)

object ChatMessageId {
  implicit val jsonWrites = new Writes[ChatMessageId] {
    def writes(artistId: ChatMessageId) = JsNumber(artistId.value)
  }

  implicit val columnMapper: BaseColumnType[ChatMessageId] = MappedColumnType.base[ChatMessageId, Int](
    _.value,
    ChatMessageId.apply
  )
}
