package model
package db

import org.joda.time.DateTime

final case class Song(
  id: SongId = SongId(0),
  artistId: ArtistId,
  secondArtistId: Option[ArtistId],
  albumId: AlbumId,
  title: String,
  aliases: Option[String],
  lyrics: Option[String],
  languageId: Option[String],
  leadVocals: Option[String],
  notes: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  spotifyId: Option[String],
  lastUpdate: DateTime = DateTime.now()
)
