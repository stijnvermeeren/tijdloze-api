package util

import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import javax.inject.{Inject, Singleton}
import model.api.{CoreAlbum, CoreArtist, CoreSong, CurrentList, CurrentListEntry}
import model.db.dao.{AlbumDAO, ArtistDAO, ListEntryDAO, ListExitDAO, SongDAO, YearDAO}
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class CurrentListUtil @Inject()(
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  songDAO: SongDAO,
  listEntryDAO: ListEntryDAO,
  listExitDAO: ListExitDAO,
  yearDAO: YearDAO
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

  private def load(): Future[CurrentList] = {
    yearDAO.maxYear() flatMap {
      case Some(year) =>
        for {
          entries <- listEntryDAO.getByYear(year)
          exits <- listExitDAO.getByYear(year)
          newSongs <- songDAO.newSongs(year)
          newAlbums <- albumDAO.newAlbums(year)
          newArtists <- artistDAO.newArtists(year)
        } yield {
          CurrentList(
            year = year,
            entries = entries.map(CurrentListEntry.fromDb),
            exitSongIds = exits.map(_.songId),
            newSongs = newSongs.map(song => CoreSong.fromDb(song, entries.filter(_.songId == song.id))),
            newAlbums = newAlbums.map(CoreAlbum.fromDb),
            newArtists = newArtists.map(CoreArtist.fromDb),
          )
        }
      case None =>
        Future.failed(new Exception("Current year not found."))
    }
  }

  def refresh(): Unit = {
    load() map { currentList =>
      currentListActorRef ! Json.toJson(currentList)
    } recover {
      case e: Exception => logger.error("Error while loading current list", e)
    }
  }
}
