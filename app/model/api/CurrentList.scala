package model.api

import model.SongId
import model.db.ListEntry
import play.api.libs.json.Json

final case class CurrentList(
  year: Int,
  entries: Seq[CurrentListEntry],
  newSongs: Seq[Song],
  newAlbums: Seq[Album],
  newArtists: Seq[Artist]
)

object CurrentList {
  implicit val jsonWrites = Json.writes[CurrentList]
}

final case class CurrentListEntry(
  position: Int,
  songId: SongId,
)

object CurrentListEntry {
  implicit val jsonWrites = Json.writes[CurrentListEntry]

  def fromDb(listEntry: ListEntry): CurrentListEntry = {
    CurrentListEntry(
      position = listEntry.position,
      songId = listEntry.songId
    )
  }
}
