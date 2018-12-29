package model.api

import play.api.libs.json.Json

final case class TextSave(
  text: String
)

object TextSave {
  implicit val jsonReads = Json.reads[TextSave]
}
