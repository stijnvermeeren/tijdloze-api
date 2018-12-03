package controllers

import javax.inject._
import model.Year
import model.api._
import model.db.dao.{AlbumDAO, ArtistDAO, ListEntryDAO, SongDAO}
import play.api.cache.Cached
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CoreDataController @Inject()(albumDAO: AlbumDAO, artistDAO: ArtistDAO, songDAO: SongDAO, listEntryDAO: ListEntryDAO, cached: Cached) extends InjectedController {
  def get() = cached("coreData") {
    Action.async { implicit rs =>
      for {
        artists <- artistDAO.getAll()
        albums <- albumDAO.getAll()
        songs <- songDAO.getAll()
        entries <- listEntryDAO.getAll()
      } yield {
        val groupedEntries = entries.groupBy(_.songId)
        Ok(Json.toJson(CoreData(
          artists = artists.map(CoreArtist.fromDb),
          albums = albums.map(CoreAlbum.fromDb),
          songs = songs map { song =>
            CoreSong.fromDb(song, groupedEntries.getOrElse(song.id, Seq.empty))
          },
          countries = Country.all,
          languages = Language.all,
          vocalsGenders = VocalsGender.all,
          years = Year.all
        )))
      }
    }
  }
}
