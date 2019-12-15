package model.db.dao

import com.github.tototoshi.slick.MySQLJodaSupport._
import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables
import model.db.ChatTicket
import org.joda.time.DateTime
import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChatTicketDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def create(userId: String): Future[String] =  {
    val uuid = UUID.randomUUID().toString

    db run {
      ChatTicketTable += ChatTicket(ticket = uuid, userId = userId)
    } map { _ =>
      uuid
    }
  }

  def use(ticket: String): Future[Option[String]] = {
    val maxAgeSeconds = 30

    db run {
      ChatTicketTable
        .filter(_.ticket === ticket)
        .filter(_.created > DateTime.now().minusSeconds(maxAgeSeconds))
        .filter(_.used.isEmpty)
        .result
        .headOption
    } flatMap {
      case Some(ticketEntry) =>
        db run {
          ChatTicketTable
            .filter(_.ticket === ticket)
            .filter(_.used.isEmpty)
            .map(_.used)
            .update(Some(DateTime.now()))
        } map (_ => Some(ticketEntry.userId))
      case None =>
        Future.successful(None)
    }
  }
}
