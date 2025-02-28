package model
package api

import play.api.libs.json.Json

final case class Song(
  id: SongId,
  artistId: ArtistId,
  secondArtistId: Option[ArtistId],
  albumId: AlbumId,
  title: String,
  aliases: Option[String],
  lyrics: Option[String],
  languageId: Option[String],
  leadVocals: Option[String],
  notes: Option[String],
  musicbrainzRecordingId: Option[String],
  musicbrainzWorkId: Option[String],
  wikidataId: Option[String],
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  spotifyId: Option[String],
)

object Song {
  def fromDb(dbSong: db.Song): Song = {
    Song(
      id = dbSong.id,
      artistId = dbSong.artistId,
      secondArtistId = dbSong.secondArtistId,
      albumId = dbSong.albumId,
      title = dbSong.title,
      aliases = dbSong.aliases,
      lyrics = dbSong.lyrics,
      languageId = dbSong.languageId,
      leadVocals = dbSong.leadVocals,
      notes = dbSong.notes,
      musicbrainzRecordingId = dbSong.musicbrainzRecordingId,
      musicbrainzWorkId = dbSong.musicbrainzWorkId,
      wikidataId = dbSong.wikidataId,
      urlWikiEn = dbSong.urlWikiEn,
      urlWikiNl = dbSong.urlWikiNl,
      spotifyId = dbSong.spotifyId
    )
  }

  implicit val jsonWrites = Json.writes[Song]
}
