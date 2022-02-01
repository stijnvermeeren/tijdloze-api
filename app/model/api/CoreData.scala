package model
package api

import play.api.libs.json._

final case class CoreArtist(
  id: ArtistId,
  namePrefix: Option[String],
  name: String,
  countryId: Option[String]
)

object CoreArtist {
  def fromDb(artist: db.Artist): CoreArtist = {
    CoreArtist(
      id = artist.id,
      namePrefix = artist.namePrefix,
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
  secondArtistId: Option[ArtistId],
  albumId: AlbumId,
  title: String,
  languageId: Option[String],
  leadVocals: Option[String],
  positions: Map[String, Int]
)

object CoreSong {
  def fromDb(song: db.Song, entries: Seq[db.ListEntry]): CoreSong = {
    CoreSong(
      id = song.id,
      artistId = song.artistId,
      secondArtistId = song.secondArtistId,
      albumId = song.albumId,
      title = song.title,
      languageId = song.languageId,
      leadVocals = song.leadVocals,
      positions = entries.map(entry => entry.year.toString.takeRight(2) -> entry.position).toMap
    )
  }

  implicit val jsonWrites = Json.writes[CoreSong]
}

final case class CoreList(
  year: Int,
  songIds: Seq[SongId]
)

object CoreList {
  implicit val jsonWrites = Json.writes[CoreList]
}

final case class CoreData(
  artists: Seq[CoreArtist],
  albums: Seq[CoreAlbum],
  songs: Seq[CoreSong],
  countries: Seq[Country],
  languages: Seq[Language],
  vocalsGenders: Seq[VocalsGender],
  years: Seq[Int],
  lists: Seq[CoreList],
  exitSongIds: Seq[SongId]
)

object CoreData {
  implicit val jsonWrites = Json.writes[CoreData]
}
