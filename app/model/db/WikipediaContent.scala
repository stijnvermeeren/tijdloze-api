package model.db

import org.joda.time.DateTime

final case class WikipediaContent(
  url: String,
  content: String,
  lastUpdate: DateTime = DateTime.now()
)
