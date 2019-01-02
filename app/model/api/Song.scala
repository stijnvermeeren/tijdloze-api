package model
package api

import play.api.libs.json.Json

final case class Song(
  id: SongId,
  artistId: ArtistId,
  albumId: AlbumId,
  title: String,
  lyrics: String,
  languageId: String,
  leadVocals: String,
  notes: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  spotifyId: Option[String],
)

object Song {
  def fromDb(dbSong: db.Song): Song = {
    Song(
      id = dbSong.id,
      artistId = dbSong.artistId,
      albumId = dbSong.albumId,
      title = dbSong.title,
      lyrics = dbSong.lyrics,
      languageId = dbSong.languageId,
      leadVocals = dbSong.leadVocals,
      notes = dbSong.notes,
      urlWikiEn = dbSong.urlWikiEn,
      urlWikiNl = dbSong.urlWikiNl,
      spotifyId = dbSong.spotifyId
    )
  }

  implicit val jsonWrites = Json.writes[Song]
}
