package model
package db

final case class Album(
  id: AlbumId = AlbumId(0),
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  urlWikiEn: String,
  urlWikiNl: String,
  urlAllMusic: String,
  edit: Boolean = false
)
