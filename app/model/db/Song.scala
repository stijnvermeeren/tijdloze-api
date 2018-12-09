package model
package db

import org.joda.time.DateTime

final case class Song(
  id: SongId = SongId(0),
  artistId: ArtistId,
  albumId: AlbumId,
  title: String,
  exitCurrent: Boolean = false,
  lyrics: String,
  languageId: String,
  leadVocals: String,
  notes: String,
  urlWikiEn: String,
  urlWikiNl: String,
  spotifyId: Option[String],
  edit: Boolean = false,
  lastUpdate: DateTime = DateTime.now()
)
