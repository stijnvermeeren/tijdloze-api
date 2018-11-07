package model
package db

final case class ListEntry(
  id: ListEntryId,
  songId: SongId,
  year: Int,
  position: Int
)
