package model
package db
package dao.table
import slick.jdbc.MySQLProfile.api._

class ListEntryTable(tag: Tag) extends Table[ListEntry](tag, "list_entry") {
  val id = column[ListEntryId]("id", O.AutoInc, O.PrimaryKey)
  val songId = column[SongId]("song_id")
  val year = column[Int]("year")
  val position = column[Int]("position")

  def * = (id, songId, year, position) <>
    ((ListEntry.apply _).tupled, ListEntry.unapply)
}
