package model.db
package dao.table

import slick.jdbc.MySQLProfile.api._

class TextTable(tag: Tag) extends Table[Text](tag, "text") {
  val key = column[String]("key", O.PrimaryKey)
  val value = column[String]("value")

  def * = (key, value) <>
    ((Text.apply _).tupled, Text.unapply)
}
