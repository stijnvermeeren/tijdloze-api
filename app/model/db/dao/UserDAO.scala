package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.UserSave
import model.db.dao.table.AllTables
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(id: String): Future[Option[User]] = {
    db run {
      UserTable.filter(_.id === id).result.headOption
    }
  }

  def save(id: String, userSave: UserSave): Future[Unit] = {
    db run {
      UserTable
        .filter(_.id === id)
        .map(user => (
          user.name,
          user.firstName,
          user.lastName,
          user.nickname,
          user.email,
          user.emailVerified,
          user.lastSeen
        ))
        .update(
          userSave.name,
          userSave.firstName,
          userSave.lastName,
          userSave.nickname,
          userSave.email,
          userSave.emailVerified.getOrElse(false),
          DateTime.now()
        )
    } flatMap { result =>
      if (result == 1) {
        Future.successful(())
      } else {
        db run {
          UserTable += User(
            id = id,
            name = userSave.name,
            firstName = userSave.firstName,
            lastName = userSave.lastName,
            nickname = userSave.nickname,
            email = userSave.email,
            emailVerified = userSave.emailVerified.getOrElse(false)
          )
        } map (_ => ())
      }
    }
  }

  def setDisplayName(id: String, displayName: String): Future[Unit] = {
    db run {
      UserTable
        .filter(_.id === id)
        .map(_.displayName)
        .update(Some(displayName))
    } map (_ => ())
  }
}
