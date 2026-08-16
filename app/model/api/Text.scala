package model
package api

import play.api.libs.json.{Json, OWrites}

final case class Text(
  key: String,
  value: String
)

object Text {
  def fromDb(dbText: db.Text): Text = {
    Text(
      key = dbText.key,
      value = dbText.value
    )
  }

  implicit val jsonWrites: OWrites[Text] = Json.writes[Text]
}
