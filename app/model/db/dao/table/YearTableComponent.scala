package model.db
package dao.table

private[table] trait YearTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class YearTable(tag: Tag) extends Table[Year](tag, "year") {
    val year = column[Int]("year", O.PrimaryKey)

    def * = year <>
      (Year.apply, Year.unapply)
  }

  val YearTable = TableQuery[YearTable]
}
