package model
package db
package dao
package table

private[table] trait AlbumTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class AlbumTable(tag: Tag) extends Table[Album](tag, "album") {
    val id = column[AlbumId]("id", O.AutoInc, O.PrimaryKey)
    val artistId = column[ArtistId]("artist_id")
    val title = column[String]("title")
    val releaseYear = column[Int]("release_year")
    val urlWikiEn = column[Option[String]]("url_wikien")
    val urlWikiNl = column[Option[String]]("url_wikinl")
    val urlAllMusic = column[Option[String]]("url_allmusic")

    def * = (id, artistId, title, releaseYear, urlWikiEn, urlWikiNl, urlAllMusic) <>
      ((Album.apply _).tupled, Album.unapply)
  }

  val AlbumTable = TableQuery[AlbumTable]
}
