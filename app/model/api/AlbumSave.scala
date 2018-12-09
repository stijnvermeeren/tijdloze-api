package model.api

import model.ArtistId
import play.api.libs.json.Json

final case class AlbumSave(
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String]
)

object AlbumSave {
  implicit val jsonReads = Json.reads[AlbumSave]
}
