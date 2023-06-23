package model.db.dao

import javax.inject.{Inject, Singleton}
import model.db.LogUserDisplayName
import model.db.dao.table.LogUserDisplayNameTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LogUserDisplayNameDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val logUserDisplayNameTable = TableQuery[LogUserDisplayNameTable]

  def save(userId: String, displayName: String): Future[Unit] = {
    db run {
      logUserDisplayNameTable += LogUserDisplayName(
        userId = userId,
        displayName = displayName
      )
    } map (_ => ())
  }
}
