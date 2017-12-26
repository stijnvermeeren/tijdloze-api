package model
package db
package dao
package table

private[table] trait AlbumTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class AlbumTable(tag: Tag) extends Table[Album](tag, "album") {
    val id = column[AlbumId]("id", O.AutoInc, O.PrimaryKey)
    val artistId = column[ArtistId]("artiest_id")
    val title = column[String]("titel")
    val year = column[Int]("jaartal")
    val urlWikiEn = column[String]("url_wikien")
    val urlWikiNl = column[String]("url_wikinl")
    val urlAllmusic = column[String]("url_allmusic")
    val edit = column[Boolean]("edit")

    def * = (id, artistId, title, year, urlWikiEn, urlWikiNl, urlAllmusic, edit) <>
      ((Album.apply _).tupled, Album.unapply)
  }

  val AlbumTable = TableQuery[AlbumTable]
}
