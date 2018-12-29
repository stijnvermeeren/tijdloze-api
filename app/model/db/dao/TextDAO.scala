package model.db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TextDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(key: String): Future[Option[Text]] = {
    db run {
      TextTable.filter(_.key === key).result.headOption
    }
  }

  def save(key: String, value: String): Future[Unit] = {
    val update = db run {
      TextTable
        .filter(_.key === key)
        .map(_.value)
        .update(value)
    }

    update flatMap { updateCount =>
      if (updateCount < 1) {
        db run {
          TextTable += Text(key = key, value = value)
        } map (_ => ())
      } else {
        Future.successful(())
      }
    }
  }
}
