package model
package db

import org.joda.time.DateTime

final case class Song(
  id: SongId = SongId(0),
  artistId: ArtistId,
  albumId: AlbumId,
  title: String,
  lyrics: Option[String],
  languageId: String,
  leadVocals: String,
  notes: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  spotifyId: Option[String],
  lastUpdate: DateTime = DateTime.now()
)
