package model.db.dao

import model.{CrawlSong, CrawlSongId, SongCrawlField, SongId}
import model.db.dao.table.CrawlSongTable
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CrawlSongDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val crawlSongTable = TableQuery[CrawlSongTable]

  def findById(id: CrawlSongId): Future[Option[CrawlSong]] = {
    db run {
      crawlSongTable.filter(_.id === id).result.headOption
    }
  }

  def find(songId: SongId, field: SongCrawlField, value: Option[String]): Future[Option[CrawlSong]] = {
    db run {
      crawlSongTable
        .filter(_.songId === songId)
        .filter(_.field === field)
        .filter(_.value === value)
        .result
        .headOption
    }
  }

  def delete(crawlSongId: CrawlSongId): Future[Int] = {
    db run {
      crawlSongTable.filter(_.id === crawlSongId).delete
    }
  }

  def saveAuto(
    songId: SongId,
    field: SongCrawlField,
    value: Option[String],
    comment: Option[String],
    isAccepted: Boolean
  ): Future[Int] = {
    val newCrawl = CrawlSong(
      songId = songId,
      crawlDate = DateTime.now(),
      field = field,
      value = value,
      comment = comment,
      isAuto = true,
      isAccepted = Some(isAccepted)
    )

    find(songId, field, value) flatMap {
      case Some(existingCrawl) if existingCrawl.isAuto && existingCrawl.isAccepted.contains(isAccepted) =>
        Future.successful(0)
      case Some(existingCrawl) =>
        delete(existingCrawl.id) flatMap { _ =>
          db run {
            crawlSongTable += newCrawl
          }
        }
      case None =>
        db run {
          crawlSongTable += newCrawl
        }
    }
  }

  def savePending(
    songId: SongId,
    field: SongCrawlField,
    value: Option[String],
    comment: Option[String]
  ): Future[Int] = {
    val newCrawl = CrawlSong(
      songId = songId,
      crawlDate = DateTime.now(),
      field = field,
      value = value,
      comment = comment,
      isAuto = false,
      isAccepted = None
    )

    find(songId, field, value) flatMap {
      case Some(existingCrawl) =>
        Future.successful(0)
      case None =>
        db run {
          crawlSongTable += newCrawl
        }
    }
  }

  def getFirstPending(): Future[Option[CrawlSong]] = {
    db run {
      crawlSongTable.filter(_.isAccepted.isEmpty).result.headOption
    }
  }

  def accept(crawlSongId: CrawlSongId): Future[Int] = {
    db run {
      crawlSongTable.filter(_.id === crawlSongId).map(_.isAccepted).update(Some(true))
    }
  }

  def reject(crawlSongId: CrawlSongId): Future[Int] = {
    db run {
      crawlSongTable.filter(_.id === crawlSongId).map(_.isAccepted).update(Some(false))
    }
  }
}
