package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.UserSave
import model.db.dao.table.AllTables

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
        .map(user => (user.firstName, user.lastName, user.email))
        .update(userSave.firstName, userSave.lastName, userSave.email)
    } flatMap { result =>
      if (result > 1) {
        Future.successful(())
      } else {
        db run {
          UserTable += User(
            id = id,
            firstName = userSave.firstName,
            lastName = userSave.lastName,
            email = userSave.email
          )
        } map (_ => ())
      }
    }
  }
}
