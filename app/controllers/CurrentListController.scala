package controllers

import javax.inject._
import model.api._
import model.db.dao.{AlbumDAO, ArtistDAO, ListEntryDAO, SongDAO}
import play.api.cache.Cached
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CurrentListController @Inject()(
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  songDAO: SongDAO,
  listEntryDAO: ListEntryDAO,
  cached: Cached
) extends InjectedController {

  def get() = cached("currentList") {
    Action.async { implicit rs =>
      for {
        year <- listEntryDAO.currentYear()
        entries <- listEntryDAO.getByYear(year)
        exits <- songDAO.exits()
        newSongs <- songDAO.newSongs(year)
        newAlbums <- albumDAO.newAlbums(year)
        newArtists <- artistDAO.newArtists(year)
      } yield {
        Ok(Json.toJson(CurrentList(
          year = year,
          entries = entries.map(CurrentListEntry.fromDb),
          exits = exits,
          newSongs = newSongs.map(song => CoreSong.fromDb(song, entries.filter(_.songId == song.id))),
          newAlbums = newAlbums.map(CoreAlbum.fromDb),
          newArtists = newArtists.map(CoreArtist.fromDb),
        )))
      }
    }
  }
}
