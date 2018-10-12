package model
package api

import play.api.libs.json._

final case class CoreArtist(
  id: ArtistId,
  firstName: String,
  name: String,
  countryId: String
)

object CoreArtist {
  def fromDb(artist: db.Artist): CoreArtist = {
    CoreArtist(
      id = artist.id,
      firstName = artist.firstName,
      name = artist.name,
      countryId = artist.countryId
    )
  }

  implicit val jsonWrites = Json.writes[CoreArtist]
}

final case class CoreAlbum(
  id: AlbumId,
  artistId: ArtistId,
  title: String,
  releaseYear: Int
)

object CoreAlbum {
  def fromDb(album: db.Album): CoreAlbum = {
    CoreAlbum(
      id = album.id,
      artistId = album.artistId,
      title = album.title,
      releaseYear = album.releaseYear
    )
  }

  implicit val jsonWrites = Json.writes[CoreAlbum]
}

final case class CoreSong(
  id: SongId,
  artistId: ArtistId,
  albumId: AlbumId,
  title: String,
  languageId: String,
  leadVocals: String,
  positions: Map[String, Int],
  exitCurrent: Boolean
)

object CoreSong {
  def fromDb(song: db.Song): CoreSong = {
    CoreSong(
      id = song.id,
      artistId = song.artistId,
      albumId = song.albumId,
      title = song.title,
      languageId = song.languageId,
      leadVocals = song.leadVocals,
      positions = song.positions,
      exitCurrent = song.exitCurrent
    )
  }

  implicit val jsonWrites = Json.writes[CoreSong]
}

final case class CoreData(
  artists: Seq[CoreArtist],
  albums: Seq[CoreAlbum],
  songs: Seq[CoreSong],
  countries: Seq[Country],
  languages: Seq[Language],
  vocalsGenders: Seq[VocalsGender],
  years: Seq[Int]
)

object CoreData {
  implicit val jsonWrites = Json.writes[CoreData]
}
