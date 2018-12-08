package model
package db

final case class ListEntry(
  id: ListEntryId = ListEntryId(0),
  songId: SongId,
  year: Int,
  position: Int
)
