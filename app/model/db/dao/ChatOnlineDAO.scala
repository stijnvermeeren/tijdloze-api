package model.db.dao

import javax.inject.{Inject, Singleton}
import model.db.{ChatOnline, User}
import model.db.dao.table.{ChatOnlineTable, UserTable}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.github.tototoshi.slick.MySQLJodaSupport._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

@Singleton
class ChatOnlineDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val chatOnlineTable = TableQuery[ChatOnlineTable]
  val userTable = TableQuery[UserTable]

  def save(userId: String): Future[Unit] =  {
    db run {
      chatOnlineTable
        .filter(_.userId === userId)
        .map(_.lastSeen)
        .update(DateTime.now())
    } flatMap { result =>
      if (result == 1) {
        Future.successful(())
      } else {
        db run {
          chatOnlineTable += ChatOnline(
            userId = userId
          )
        } map (_ => ())
      }
    }
  }

  def list(maxAgeSeconds: Int): Future[Seq[User]] = {
    db run {
      val query = for {
        chatOnline <- chatOnlineTable.filter(_.lastSeen > DateTime.now().minusSeconds(maxAgeSeconds))
        user <- userTable.filter(_.id === chatOnline.userId)
      } yield user

      query.result
    }
  }
}
