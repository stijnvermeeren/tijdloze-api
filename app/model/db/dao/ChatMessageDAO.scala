package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.AllTables

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChatMessageDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def save(userId: String, message: String): Future[ChatMessage] = {
    val newMessage = ChatMessage(userId = userId, message = message)
    db run {
      (ChatMessageTable returning ChatMessageTable.map(_.id)) += newMessage
    } map { messageId =>
      newMessage.copy(id = messageId)
    }
  }

  def list(): Future[Seq[(Comment, User)]] = {
    db run {
      val joinedQuery = CommentTable join UserTable on (_.userId === _.id)
      joinedQuery.sortBy(_._1.id).result
    }
  }
}
