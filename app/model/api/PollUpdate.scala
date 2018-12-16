package model.api

final case class PollUpdate(
  question: Option[String],
  isActive: Option[Boolean],
  isDeleted: Option[Boolean]
)


