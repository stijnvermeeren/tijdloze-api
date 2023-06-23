package model.db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.TextTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TextDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val textTable = TableQuery[TextTable]

  def get(key: String): Future[Option[Text]] = {
    db run {
      textTable.filter(_.key === key).result.headOption
    }
  }

  def save(key: String, value: String): Future[Unit] = {
    val update = db run {
      textTable
        .filter(_.key === key)
        .map(_.value)
        .update(value)
    }

    update flatMap { updateCount =>
      if (updateCount < 1) {
        db run {
          textTable += Text(key = key, value = value)
        } map (_ => ())
      } else {
        Future.successful(())
      }
    }
  }
}
