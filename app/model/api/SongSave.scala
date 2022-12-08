package model.api

import model.{AlbumId, ArtistId}
import play.api.libs.json.Json

final case class SongSave(
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
  spotifyId: Option[String]
)

object SongSave {
  implicit val jsonReads = Json.reads[SongSave]
}
