package controllers

import javax.inject._
import model.api._
import model.db.dao._
import play.api.cache.Cached
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CoreDataController @Inject()(
  albumDAO: AlbumDAO,
  artistDAO: ArtistDAO,
  songDAO: SongDAO,
  listEntryDAO: ListEntryDAO,
  listExitDAO: ListExitDAO,
  yearDAO: YearDAO,
  cached: Cached
) extends InjectedController {

  def get() = cached("coreData") {
    Action.async { implicit rs =>
      for {
        artists <- artistDAO.getAll()
        albums <- albumDAO.getAll()
        songs <- songDAO.getAll()
        entries <- listEntryDAO.getAll()
        years <- yearDAO.getAll()
        exits <- years.lastOption match {
          case Some(maxYear) => listExitDAO.getByYear(maxYear)
          case None => Future.successful(Seq.empty)
        }
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
          years = years,
          exitSongIds = exits.map(_.songId)
        )))
      }
    }
  }
}
