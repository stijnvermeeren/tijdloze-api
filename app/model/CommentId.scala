package model

import play.api.libs.json.{JsNumber, Writes}
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

  implicit val pathBindable: PathBindable[CommentId] = {
    PathBindable.bindableInt.transform(CommentId.apply, _.value)
  }
}
