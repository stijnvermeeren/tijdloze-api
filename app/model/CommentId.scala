package model

import play.api.libs.json.{JsNumber, Writes}
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

final case class CommentId(value: Int) extends MappedTo[Int]

object CommentId {
  implicit val jsonWrites = new Writes[CommentId] {
    def writes(commentId: CommentId) = JsNumber(commentId.value)
  }

  implicit val pathBindable: PathBindable[CommentId] = {
    PathBindable.bindableInt.transform(CommentId.apply, _.value)
  }
}
