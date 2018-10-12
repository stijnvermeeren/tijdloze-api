package controllers

import javax.inject._

import model.Year
import model.api._
import model.db.dao.{AlbumDAO, ArtistDAO, SongDAO}
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CoreDataController @Inject()(albumDAO: AlbumDAO, artistDAO: ArtistDAO, songDAO: SongDAO) extends InjectedController {
  def get() = Action.async { implicit rs =>
    for {
      artists <- artistDAO.getAll()
      albums <- albumDAO.getAll()
      songs <- songDAO.getAll()
    } yield {
      Ok(Json.toJson(CoreData(
        artists = artists.map(CoreArtist.fromDb),
        albums = albums.map(CoreAlbum.fromDb),
        songs = songs.map(CoreSong.fromDb),
        countries = Country.all,
        languages = Language.all,
        vocalsGenders = VocalsGender.all,
        years = Year.all
      )))
    }
  }
}
