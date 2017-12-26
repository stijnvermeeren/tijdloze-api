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
  notes: String,
  urlWikiEn: String,
  urlWikiNl: String
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
      notes = dbSong.notes,
      urlWikiEn = dbSong.urlWikiEn,
      urlWikiNl = dbSong.urlWikiNl
    )
  }

  implicit val jsonWrites = Json.writes[Song]
}
