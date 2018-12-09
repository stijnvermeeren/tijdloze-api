package model
package db
package dao
package table

private[table] trait ArtistTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artiest") {
    val id = column[ArtistId]("id", O.AutoInc, O.PrimaryKey)
    val firstName = column[String]("voornaam")
    val name = column[String]("achternaam")
    val countryId = column[String]("land_afkorting")
    val notes = column[String]("opmerkingen")
    val urlOfficial = column[String]("url_official")
    val urlWikiEn = column[String]("url_wikien")
    val urlWikiNl = column[String]("url_wikinl")
    val urlAllMusic = column[String]("url_allmusic")
    val edit = column[Boolean]("edit")

    def * = (id, firstName, name, countryId, notes, urlOfficial, urlWikiEn, urlWikiNl, urlAllMusic, edit) <>
      ((Artist.apply _).tupled, Artist.unapply)
  }

  val ArtistTable = TableQuery[ArtistTable]
}
