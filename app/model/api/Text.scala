package model
package api

import play.api.libs.json.Json

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

  implicit val jsonWrites = Json.writes[Text]
}
