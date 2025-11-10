package model.api

import model.db
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json.JodaWrites.JodaDateTimeWrites

final case class WikipediaContent(
  url: String,
  content: String,
  lastUpdate: DateTime
)

object WikipediaContent {
  implicit val jsonWrites = Json.writes[WikipediaContent]

  def fromDb(dbContent: db.WikipediaContent): WikipediaContent = {
    WikipediaContent(
      url = dbContent.url,
      content = dbContent.content,
      lastUpdate = dbContent.lastUpdate
    )
  }
}

