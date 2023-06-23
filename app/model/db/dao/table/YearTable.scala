package model.db
package dao.table

import slick.jdbc.MySQLProfile.api._

class YearTable(tag: Tag) extends Table[Year](tag, "year") {
  val year = column[Int]("year", O.PrimaryKey)

  def * = year <>
    (Year.apply, Year.unapply)
}
