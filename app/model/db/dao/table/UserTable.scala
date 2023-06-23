package model
package db
package dao
package table

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.jdbc.MySQLProfile.api._

class UserTable(tag: Tag) extends Table[User](tag, "user") {
  val id = column[String]("id", O.PrimaryKey)
  val displayName = column[Option[String]]("display_name")
  val name = column[Option[String]]("name")
  val firstName = column[Option[String]]("first_name")
  val lastName = column[Option[String]]("last_name")
  val nickname = column[Option[String]]("nickname")
  val email = column[Option[String]]("email")
  val emailVerified = column[Boolean]("email_verified")
  val created = column[DateTime]("created")
  val lastSeen = column[DateTime]("last_seen")
  val isAdmin = column[Boolean]("is_admin")
  val isBlocked = column[Boolean]("is_blocked")

  def * = (id, displayName, name, firstName, lastName, nickname, email, emailVerified, created, lastSeen, isAdmin, isBlocked) <>
    ((User.apply _).tupled, User.unapply)
}
