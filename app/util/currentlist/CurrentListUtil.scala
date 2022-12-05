package util.currentlist

import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.Materializer
import model.{AlbumId, ArtistId, SongId}
import model.api.{Album, Artist, Poll, Song}
import model.db.dao._
import play.api.Logger
import play.api.libs.json._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class CurrentListUtil @Inject()(
  listEntryDAO: ListEntryDAO,
  listExitDAO: ListExitDAO,
  pollDAO: PollDAO
)(implicit mat: Materializer) {
  val logger = Logger(getClass)

  private val currentListSource = Source.queue[JsValue](bufferSize = 10)

  private val (currentListQueue, source) = currentListSource.toMat(BroadcastHub.sink[JsValue])(Keep.both).run()

  // Keeps the last update available for sending immediately to new connections
  private val lastSentSource = source
    .expand(Iterator.continually(_))
    // Ensure the lastSentSource does not backpressure the stream.
    // Need to throttle this to avoid draining the CPU.
    // This is a hack and won't work well if we have a bigger throughput.
    .throttle(elements = 10, per = 1.second)
    .toMat(BroadcastHub.sink)(Keep.right).run() // allow individual users to connect dynamically */

  // Keep draining the lastSentSource so that it never backpressures.
  lastSentSource.runWith(Sink.ignore)

  def currentListFlow() = Flow[JsValue]
    .via(Flow.fromSinkAndSource(
      Sink.ignore,
      lastSentSource.take(1) concat source
    ))
    .log("chatFlow")


  def updateSong(song: Song): Unit = {
    currentListQueue.offer(Json.toJson(SongUpdate(song)))
  }

  def deleteSong(songId: SongId): Unit = {
    currentListQueue.offer(Json.toJson(SongDelete(songId)))
  }

  def updateAlbum(album: Album): Unit = {
    currentListQueue.offer(Json.toJson(AlbumUpdate(album)))
  }

  def deleteAlbum(albumId: AlbumId): Unit = {
    currentListQueue.offer(Json.toJson(AlbumDelete(albumId)))
  }

  def updateArtist(artist: Artist): Unit = {
    currentListQueue.offer(Json.toJson(ArtistUpdate(artist)))
  }

  def deleteArtist(artistId: ArtistId): Unit = {
    currentListQueue.offer(Json.toJson(ArtistDelete(artistId)))
  }

  def updateEntry(year: Int, position: Int, songId: Option[SongId]): Unit = {
    currentListQueue.offer(Json.toJson(ListEntryUpdate(year, position, songId)))
  }

  def updateCurrentYear(year: Int): Unit = {
    val data = for {
      exits <- listExitDAO.getByYear(year)
    } yield CurrentYearUpdate(
      currentYear = year,
      exitSongIds = exits.map(_.songId)
    )

    data map { currentList =>
      currentListQueue.offer(Json.toJson(currentList))
    } recover {
      case e: Exception => logger.error(s"Error while loading list of year ${year}", e)
    }
  }

  def updateLatestPoll(): Unit = {
    pollDAO.getLatest() map {
      case Some((dbPoll, dbAnswers)) =>
        val apiPoll = Poll.fromDb(dbPoll, dbAnswers)
        currentListQueue.offer(Json.toJson(PollUpdate(apiPoll)))
      case None =>
        // No active poll
    }

  }
}
