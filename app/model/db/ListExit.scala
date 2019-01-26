package model
package db

final case class ListExit(
  id: ListExitId = ListExitId(0),
  songId: SongId,
  year: Int
)
