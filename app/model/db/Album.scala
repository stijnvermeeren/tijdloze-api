package model
package db

final case class Album(
  id: AlbumId = AlbumId(0),
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String],
  spotifyId: Option[String],
  wikidataId: Option[String],
  musicbrainzId: Option[String],
  cover: Option[String],
  isSingle: Boolean = false,
  isSoundtrack: Boolean = false
)
