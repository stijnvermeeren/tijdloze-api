package model.api

import play.api.libs.json.Json

final case class ContactForm(
  name: String,
  email: Option[String],
  message: String,
  debug: Boolean = false
)

object ContactForm {
  implicit val jsonReads = Json.reads[ContactForm]
}
