package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.db.dao.table.ChatMessageTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChatMessageDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val chatMessageTable = TableQuery[ChatMessageTable]

  def save(userId: String, message: String): Future[ChatMessage] = {
    val newMessage = ChatMessage(userId = userId, message = message)
    db run {
      (chatMessageTable returning chatMessageTable.map(_.id)) += newMessage
    } map { messageId =>
      newMessage.copy(id = messageId)
    }
  }
}
