package model
package db

import org.joda.time.DateTime

final case class Song(
  id: SongId,
  artistId: ArtistId,
  albumId: AlbumId,
  title: String,
  exitCurrent: Boolean,
  lyrics: String,
  languageId: String,
  leadVocals: String,
  notes: String,
  urlWikiEn: String,
  urlWikiNl: String,
  spotifyId: Option[String],
  edit: Boolean,
  lastUpdate: DateTime
)
