package model.db.dao

import javax.inject.{Inject, Singleton}
import model.db.LogUserDisplayName
import model.db.dao.table.AllTables

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LogUserDisplayNameDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def save(userId: String, displayName: String): Future[Unit] = {
    db run {
      LogUserDisplayNameTable += LogUserDisplayName(
        userId = userId,
        displayName = displayName
      )
    } map (_ => ())
  }
}
