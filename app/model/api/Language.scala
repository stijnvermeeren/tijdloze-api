package model.api

import play.api.libs.json.Json

final case class Language(
  id: String,
  name: String
)

object Language {
  val German = Language("dui", "Duits")
  val English = Language("eng", "Engels")
  val Dutch = Language("ned", "Nederlands")
  val French = Language("fra", "Frans")
  val Icelandic = Language("ijs", "IJslands")
  val Portuguese = Language("por", "Portugees")
  val Spanish = Language("spa", "Spaans")
  val Instrumental = Language("ins", "Instrumentaal")

  val all = Seq(German, English, Dutch, French, Icelandic, Portuguese, Spanish, Instrumental)

  implicit val jsonWrites = Json.writes[Language]
}
