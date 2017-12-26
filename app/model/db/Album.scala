package model
package db

final case class Album(
  id: AlbumId,
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  urlWikiEn: String,
  urlWikiNl: String,
  urlAllmusic: String,
  edit: Boolean
)
