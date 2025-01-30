package model

import slick.jdbc.H2Profile.api._

final case class CommentVersionId(value: Int)

object CommentVersionId {
  implicit val columnMapper: BaseColumnType[CommentVersionId] = MappedColumnType.base[CommentVersionId, Int](
    _.value,
    CommentVersionId.apply
  )
}
