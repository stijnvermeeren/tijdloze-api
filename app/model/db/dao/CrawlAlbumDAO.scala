package model
package db
package dao

import model.db.dao.table.CrawlAlbumTable
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CrawlAlbumDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val crawlAlbumTable = TableQuery[CrawlAlbumTable]

  def findById(id: CrawlAlbumId): Future[Option[CrawlAlbum]] = {
    db run {
      crawlAlbumTable.filter(_.id === id).result.headOption
    }
  }

  def find(albumId: AlbumId, field: AlbumCrawlField, value: Option[String]): Future[Option[CrawlAlbum]] = {
    db run {
      crawlAlbumTable
        .filter(_.albumId === albumId)
        .filter(_.field === field)
        .filter(_.value === value)
        .result
        .headOption
    }
  }

  def delete(crawlAlbumId: CrawlAlbumId): Future[Int] = {
    db run {
      crawlAlbumTable.filter(_.id === crawlAlbumId).delete
    }
  }

  def saveAuto(
    albumId: AlbumId,
    field: AlbumCrawlField,
    value: Option[String],
    comment: Option[String],
    isAccepted: Boolean
  ): Future[Int] = {
    val newCrawl = CrawlAlbum(
      albumId = albumId,
      crawlDate = DateTime.now(),
      field = field,
      value = value,
      comment = comment,
      isAuto = true,
      isAccepted = Some(isAccepted)
    )

    find(albumId, field, value) flatMap {
      case Some(existingCrawl) if existingCrawl.isAuto && existingCrawl.isAccepted.contains(isAccepted) =>
        Future.successful(0)
      case Some(existingCrawl) =>
        delete(existingCrawl.id) flatMap { _ =>
          db run {
            crawlAlbumTable += newCrawl
          }
        }
      case None =>
        db run {
          crawlAlbumTable += newCrawl
        }
    }
  }

  def savePending(
    albumId: AlbumId,
    field: AlbumCrawlField,
    value: Option[String],
    comment: Option[String]
  ): Future[Int] = {
    val newCrawl = CrawlAlbum(
      albumId = albumId,
      crawlDate = DateTime.now(),
      field = field,
      value = value,
      comment = comment,
      isAuto = false,
      isAccepted = None
    )

    find(albumId, field, value) flatMap {
      case Some(existingCrawl) =>
        Future.successful(0)
      case None =>
        db run {
          crawlAlbumTable += newCrawl
        }
    }
  }

  def getFirstPending(): Future[Option[CrawlAlbum]] = {
    db run {
      crawlAlbumTable.filter(_.isAccepted.isEmpty).result.headOption
    }
  }

  def accept(crawlAlbumId: CrawlAlbumId): Future[Int] = {
    db run {
      crawlAlbumTable.filter(_.id === crawlAlbumId).map(_.isAccepted).update(Some(true))
    }
  }

  def reject(crawlAlbumId: CrawlAlbumId): Future[Int] = {
    db run {
      crawlAlbumTable.filter(_.id === crawlAlbumId).map(_.isAccepted).update(Some(false))
    }
  }
}
