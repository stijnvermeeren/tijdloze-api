package model
package db
package dao

import model.db.dao.table.AllTables
import org.joda.time.DateTime

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CrawlArtistDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def findById(id: CrawlArtistId): Future[Option[CrawlArtist]] = {
    db run {
      CrawlArtistTable.filter(_.id === id).result.headOption
    }
  }

  def find(artistId: ArtistId, field: String, value: Option[String]): Future[Option[CrawlArtist]] = {
    db run {
      CrawlArtistTable
        .filter(_.artistId === artistId)
        .filter(_.field === field)
        .filter(_.value === value)
        .result
        .headOption
    }
  }

  def delete(crawlArtistId: CrawlArtistId): Future[Int] = {
    db run {
      CrawlArtistTable.filter(_.id === crawlArtistId).delete
    }
  }

  def saveAuto(
    artistId: ArtistId,
    field: String,
    value: Option[String],
    comment: Option[String],
    isAccepted: Boolean
  ): Future[Int] = {
    val newCrawl = CrawlArtist(
      artistId = artistId,
      crawlDate = DateTime.now(),
      field = field,
      value = value,
      comment = comment,
      isAuto = true,
      isAccepted = Some(isAccepted)
    )

    find(artistId, field, value) flatMap {
      case Some(existingCrawl) if existingCrawl.isAuto && existingCrawl.isAccepted.contains(isAccepted) =>
        Future.successful(0)
      case Some(existingCrawl) =>
        delete(existingCrawl.id) flatMap { _ =>
          db run {
            CrawlArtistTable += newCrawl
          }
        }
      case None =>
        db run {
          CrawlArtistTable += newCrawl
        }
    }
  }

  def savePending(
    artistId: ArtistId,
    field: String,
    value: Option[String],
    comment: Option[String]
  ): Future[Int] = {
    val newCrawl = CrawlArtist(
      artistId = artistId,
      crawlDate = DateTime.now(),
      field = field,
      value = value,
      comment = comment,
      isAuto = false,
      isAccepted = None
    )

    find(artistId, field, value) flatMap {
      case Some(existingCrawl) =>
        Future.successful(0)
      case None =>
        db run {
          CrawlArtistTable += newCrawl
        }
    }
  }

  def getFirstPending(): Future[Option[CrawlArtist]] = {
    db run {
      CrawlArtistTable.filter(_.isAccepted.isEmpty).result.headOption
    }
  }

  def accept(crawlArtistId: CrawlArtistId): Future[Int] = {
    db run {
      CrawlArtistTable.filter(_.id === crawlArtistId).map(_.isAccepted).update(Some(true))
    }
  }

  def reject(crawlArtistId: CrawlArtistId): Future[Int] = {
    db run {
      CrawlArtistTable.filter(_.id === crawlArtistId).map(_.isAccepted).update(Some(false))
    }
  }
}
