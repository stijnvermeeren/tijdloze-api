package model
package db

import org.joda.time.DateTime

final case class Song(
  id: SongId,
  artistId: ArtistId,
  albumId: AlbumId,
  title: String,
  positions: Map[String, Int],
  exitCurrent: Boolean,
  lyrics: String,
  languageId: String,
  notes: String,
  urlWikiEn: String,
  urlWikiNl: String,
  edit: Boolean,
  lastUpdate: DateTime
)
