package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.UserSave
import model.db.dao.table.UserTable
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val userTable = TableQuery[UserTable]

  def get(id: String): Future[Option[User]] = {
    db run {
      userTable.filter(_.id === id).result.headOption
    }
  }

  def save(id: String, userSave: UserSave): Future[Unit] = {
    db run {
      userTable
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
          userTable += User(
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
      userTable
        .filter(_.id === id)
        .map(_.displayName)
        .update(Some(displayName))
    } map (_ => ())
  }

  def setBlocked(id: String, isBlocked: Boolean): Future[Unit] = {
    db run {
      userTable
        .filter(_.id === id)
        .map(_.isBlocked)
        .update(isBlocked)
    } map (_ => ())
  }

  def listAll(): Future[Seq[User]] = {
    db run {
      userTable.sortBy(_.displayName).result
    }
  }
}
