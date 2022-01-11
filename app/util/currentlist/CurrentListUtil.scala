package util.currentlist

import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import model.api.{Album, Song, Artist}
import model.db.dao._
import play.api.Logger
import play.api.libs.json._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class CurrentListUtil @Inject()(
  listEntryDAO: ListEntryDAO,
  listExitDAO: ListExitDAO
)(implicit mat: Materializer) {
  val logger = Logger(getClass)

  private val currentListSource = Source.actorRef[JsValue](bufferSize = 1, overflowStrategy = OverflowStrategy.dropNew)

  private val (currentListActorRef, source) = currentListSource.toMat(BroadcastHub.sink[JsValue])(Keep.both).run()

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


  def refreshSong(song: Song): Unit = {
    currentListActorRef ! Json.toJson(SongUpdate(song))
  }

  def refreshAlbum(album: Album): Unit = {
    currentListActorRef ! Json.toJson(AlbumUpdate(album))
  }

  def refreshArtist(artist: Artist): Unit = {
    currentListActorRef ! Json.toJson(ArtistUpdate(artist))
  }

  def refresh(year: Int): Unit = {
    val data = for {
      entries <- listEntryDAO.getByYear(year)
      exits <- listExitDAO.getByYear(year)
    } yield ListUpdate(
      year = year,
      entries = entries.map(ListUpdateEntry.fromDb),
      exitSongIds = exits.map(_.songId)
    )

    data map { currentList =>
      currentListActorRef ! Json.toJson(currentList)
    } recover {
      case e: Exception => logger.error(s"Error while loading list of year ${year}", e)
    }
  }
}
