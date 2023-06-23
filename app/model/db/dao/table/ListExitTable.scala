package model
package db
package dao.table

import slick.jdbc.MySQLProfile.api._

class ListExitTable(tag: Tag) extends Table[ListExit](tag, "list_exit") {
  val id = column[ListExitId]("id", O.AutoInc, O.PrimaryKey)
  val songId = column[SongId]("song_id")
  val year = column[Int]("year")

  def * = (id, songId, year) <>
    ((ListExit.apply _).tupled, ListExit.unapply)
}
