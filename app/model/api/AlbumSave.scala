package model.api

import model.ArtistId
import play.api.libs.json.Json

final case class AlbumSave(
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
  isSingle: Boolean,
  isSoundtrack: Boolean
)

object AlbumSave {
  implicit val jsonReads = Json.reads[AlbumSave]
}
