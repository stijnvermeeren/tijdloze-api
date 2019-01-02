package model
package db

final case class Album(
  id: AlbumId = AlbumId(0),
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String]
)
