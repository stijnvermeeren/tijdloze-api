package model.api

import play.api.libs.json.{Json, Reads}

final case class TextSave(
  text: String
)

object TextSave {
  implicit val jsonReads: Reads[TextSave] = Json.reads[TextSave]
}
