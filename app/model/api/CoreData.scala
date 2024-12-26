package model
package api

import play.api.libs.json._

final case class CoreArtist(
  id: ArtistId,
  name: String,
  aliases: Option[String],
  countryId: Option[String]
)

object CoreArtist {
  def fromDb(artist: db.Artist): CoreArtist = {
    CoreArtist(
      id = artist.id,
      name = artist.name,
      aliases = artist.aliases,
      countryId = artist.countryId
    )
  }

  implicit val jsonWrites = Json.writes[CoreArtist]
}

final case class CoreAlbum(
  id: AlbumId,
  artistId: ArtistId,
  title: String,
  releaseYear: Int,
  cover: Option[String],
  isSingle: Option[Boolean],
  isSoundtrack: Option[Boolean],
)

object CoreAlbum {
  def fromDb(album: db.Album): CoreAlbum = {
    CoreAlbum(
      id = album.id,
      artistId = album.artistId,
      title = album.title,
      releaseYear = album.releaseYear,
      cover = album.cover,
      isSingle = Option(album.isSingle).filter(identity),
      isSoundtrack = Option(album.isSoundtrack).filter(identity),
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
  aliases: Option[String],
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
      aliases = song.aliases,
      languageId = song.languageId,
      leadVocals = song.leadVocals,
      positions = entries
        .groupBy(_.year)
        .map {
          case (year, entries) => year.toString.takeRight(2) -> entries.map(_.position).min
        }
    )
  }

  implicit val jsonWrites = Json.writes[CoreSong]
}

final case class CoreList(
  year: Int,
  songIds: Seq[Option[SongId]]
)

object CoreList {
  implicit val jsonWrites = Json.writes[CoreList]
}

final case class CoreData(
  artists: Seq[CoreArtist],
  albums: Seq[CoreAlbum],
  songs: Seq[CoreSong],
  years: Seq[Int],
  lists: Seq[CoreList],
  exitSongIds: Seq[SongId]
)

object CoreData {
  implicit val jsonWrites = Json.writes[CoreData]
}
