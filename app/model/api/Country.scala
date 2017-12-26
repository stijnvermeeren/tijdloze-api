package model.api

import play.api.libs.json.Json

final case class Country(
  id: String,
  name: String
)

object Country {
  val Australia = Country("aus", "Australië")
  val Belgium = Country("bel", "België")
  val Canada = Country("can", "Canada")
  val France = Country("fra", "Frankrijk")
  val Germany = Country("dui", "Duitsland")
  val Ireland = Country("ier", "Ierland")
  val Jamaica = Country("jam", "Jamaica")
  val Netherlands = Country("ned", "Nederland")
  val Sweden = Country("zwe", "Zweden")
  val UK = Country("gbr", "Verenigd Koninkrijk")
  val USA = Country("usa", "Verenigde Staten")

  val all = Seq(Australia, Belgium, Canada, France, Germany, Ireland, Jamaica, Netherlands, Sweden, UK, USA)

  implicit val jsonWrites = Json.writes[Country]
}
