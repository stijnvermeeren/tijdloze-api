package model
package db
package dao
package table

private[table] trait ArtistTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artist") {
    val id = column[ArtistId]("id", O.AutoInc, O.PrimaryKey)
    val namePrefix = column[Option[String]]("name_prefix")
    val name = column[String]("name")
    val countryId = column[Option[String]]("country_id")
    val notes = column[Option[String]]("notes")
    val urlOfficial = column[Option[String]]("url_official")
    val urlWikiEn = column[Option[String]]("url_wikien")
    val urlWikiNl = column[Option[String]]("url_wikinl")
    val urlAllMusic = column[Option[String]]("url_allmusic")

    def * = (id, namePrefix, name, countryId, notes, urlOfficial, urlWikiEn, urlWikiNl, urlAllMusic) <>
      ((Artist.apply _).tupled, Artist.unapply)
  }

  val ArtistTable = TableQuery[ArtistTable]
}
