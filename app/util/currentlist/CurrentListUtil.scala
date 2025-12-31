package util.currentlist

import org.apache.pekko.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import org.apache.pekko.stream.{Materializer, OverflowStrategy}
import model.{AlbumId, ArtistId, SongId}
import model.api.{Album, Artist, Poll, Song}
import model.db.dao._
import play.api.Logger
import play.api.libs.json._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CurrentListUtil @Inject()(
  listExitDAO: ListExitDAO,
  pollDAO: PollDAO
)(implicit mat: Materializer) {
  val logger = Logger(getClass)

  private val currentListSource = Source.queue[JsValue](bufferSize = 10)

  private val (currentListQueue, source) = currentListSource.toMat(BroadcastHub.sink[JsValue])(Keep.both).run()

  // Buffersize of 4 allows for creating new artist, album, song + adding entry at the same time, without immediately
  // dropping any of those messages.
  private val bufferedSource = source.buffer(4, OverflowStrategy.dropHead)

  def currentListFlow() = Flow[JsValue]
    .via(Flow.fromSinkAndSource(
      Sink.ignore,
      bufferedSource
    ))
    .log("currentListFlow")


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
