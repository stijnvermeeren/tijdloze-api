package model
package api

import play.api.libs.json.Json

final case class Album(
  id: AlbumId,
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  urlWikiEn: Option[String],
  urlWikiNl: Option[String],
  urlAllMusic: Option[String],
  spotifyId: Option[String],
  wikidataId: Option[String],
  musicbrainzId: Option[String],
  cover: Option[String]
)

object Album {
  def fromDb(dbAlbum: db.Album): Album = {
    Album(
      id = dbAlbum.id,
      artistId = dbAlbum.artistId,
      title = dbAlbum.title,
      releaseYear = dbAlbum.releaseYear,
      urlWikiEn = dbAlbum.urlWikiEn,
      urlWikiNl = dbAlbum.urlWikiNl,
      urlAllMusic = dbAlbum.urlAllMusic,
      spotifyId = dbAlbum.spotifyId,
      wikidataId = dbAlbum.wikidataId,
      musicbrainzId = dbAlbum.musicbrainzId,
      cover = dbAlbum.cover
    )
  }

  implicit val jsonWrites = Json.writes[Album]
}
