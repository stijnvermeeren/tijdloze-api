package model

import play.api.libs.json.{JsNumber, JsResult, JsValue, Reads, Writes}
import play.api.mvc.PathBindable
import slick.jdbc.H2Profile.api._

final case class CommentId(value: Int)

object CommentId {
  implicit val columnMapper: BaseColumnType[CommentId] = MappedColumnType.base[CommentId, Int](
    _.value,
    CommentId.apply
  )

  implicit val jsonWrites = new Writes[CommentId] {
    def writes(commentId: CommentId) = JsNumber(commentId.value)
  }

  implicit val jsonReads = new Reads[CommentId] {
    def reads(value: JsValue): JsResult[CommentId] = {
      value.validate[Int].map(CommentId.apply)
    }
  }

  implicit val pathBindable: PathBindable[CommentId] = {
    PathBindable.bindableInt.transform(CommentId.apply, _.value)
  }
}
