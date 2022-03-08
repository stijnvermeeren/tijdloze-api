package model.api

import play.api.libs.json.Json

final case class Country(
  id: String,
  name: String
)

object Country {
  // ISO_3166-2 codes
  val Austria = Country("at", "Oostenrijk")
  val Australia = Country("au", "Australië")
  val Belgium = Country("be", "België")
  val Canada = Country("ca", "Canada")
  val Switzerland = Country("ch", "Zwitserland")
  val Cuba = Country("cu", "Cuba")
  val Germany = Country("de", "Duitsland")
  val Denmark = Country("dk", "Denemarken")
  val France = Country("fr", "Frankrijk")
  val UK = Country("gb", "Verenigd Koninkrijk")
  val Ireland = Country("ie", "Ierland")
  val Iceland = Country("is", "IJsland")
  val Jamaica = Country("jm", "Jamaica")
  val Netherlands = Country("nl", "Nederland")
  val Norway = Country("no", "Noorwegen")
  val Spain  = Country("es", "Spanje")
  val Sweden = Country("se", "Zweden")
  val USA = Country("us", "Verenigde Staten")

  val all = Seq(
    Austria, Australia, Belgium, Canada, Switzerland, Cuba, Germany, Denmark, France, UK, Ireland, Iceland, Jamaica,
    Netherlands, Norway, Spain, Sweden, USA
  )

  implicit val jsonWrites = Json.writes[Country]
}
