package model.db
package dao.table

private[table] trait TextTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class TextTable(tag: Tag) extends Table[Text](tag, "text") {
    val key = column[String]("key", O.PrimaryKey)
    val value = column[String]("value")

    def * = (key, value) <>
      ((Text.apply _).tupled, Text.unapply)
  }

  val TextTable = TableQuery[TextTable]
}
