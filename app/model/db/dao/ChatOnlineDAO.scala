package model.db.dao

import javax.inject.{Inject, Singleton}
import model.db.{ChatOnline, User}
import model.db.dao.table.AllTables
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.github.tototoshi.slick.MySQLJodaSupport._

@Singleton
class ChatOnlineDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def save(userId: String): Future[Unit] =  {
    db run {
      ChatOnlineTable
        .filter(_.userId === userId)
        .map(_.lastSeen)
        .update(DateTime.now())
    } flatMap { result =>
      if (result == 1) {
        Future.successful(())
      } else {
        db run {
          ChatOnlineTable += ChatOnline(
            userId = userId
          )
        } map (_ => ())
      }
    }
  }

  def list(maxAgeSeconds: Int): Future[Seq[User]] = {
    db run {
      val query = for {
        chatOnline <- ChatOnlineTable.filter(_.lastSeen > DateTime.now().minusSeconds(maxAgeSeconds))
        user <- UserTable.filter(_.id === chatOnline.userId)
      } yield user

      query.result
    }
  }
}
