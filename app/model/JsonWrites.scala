package model

import org.joda.time.DateTime
import play.api.libs.json.{JodaWrites, Writes}

object JsonWrites {
  implicit val dateTimeWriter: Writes[DateTime] = {
    JodaWrites.jodaDateWrites("dd/MM/yyyy HH:mm:ss")
  }
}
