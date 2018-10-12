package model.api

import play.api.libs.json.Json

final case class VocalsGender(
  id: String,
  name: String
)

object VocalsGender {
  val Male = VocalsGender("m", "Man")
  val Female = VocalsGender("f", "Vrouw")
  val Duet = VocalsGender("x", "Duet")
  val Instrumental = VocalsGender("i", "Instrumentaal")

  val all = Seq(Male, Female, Duet, Instrumental)

  implicit val jsonWrites = Json.writes[VocalsGender]
}
