package controllers

import model.db.dao.{AlbumDAO, ArtistDAO, CrawlArtistDAO, SongDAO}
import model.db.{Artist, Song}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import util.FutureUtil
import util.wikidata.WikidataAPI

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WikidataController @Inject()(
  wikidataAPI: WikidataAPI,
  songDAO: SongDAO,
  artistDAO: ArtistDAO,
  crawlArtistDAO: CrawlArtistDAO,
  cache: AsyncCacheApi
) extends InjectedController {

  def crawlArtistsFromSpotify() = {
    def setArtistWikidataId(artist: Artist, wikidataIds: Seq[String]) = {
      def saveAuto(wikidataId: String) = crawlArtistDAO.saveAuto(
        artist.id,
        field = "wikidataId",
        value = Some(wikidataId),
        comment = Some(s"Spotify id (${artist.spotifyId.getOrElse("")})"),
        isAccepted = true
      ) flatMap { _ =>
        artistDAO.setWikidataId(artist.id, Some(wikidataId))
      } map { _ =>
        cache.remove(s"artist/${artist.id.value}")
      }

      wikidataIds.headOption.filter(_ => wikidataIds.length == 1) match {
        case Some(uniqueWikidataId) =>
          saveAuto(uniqueWikidataId)
        case _ =>
          FutureUtil.traverseSequentially(wikidataIds) { wikidataId =>
            crawlArtistDAO.savePending(
              artist.id,
              field = "wikidataId",
              value = Some(wikidataId),
              comment = Some(s"Spotify id (${artist.spotifyId.getOrElse("")})")
            )
          }
      }
    }

    Action.async { implicit request =>
      artistDAO.getAll().flatMap{ artists =>
        val artistsWithSpotifyId = artists.flatMap { artist =>
          artist.spotifyId.map(spotifyId => (artist, spotifyId))
        }
        FutureUtil.traverseSequentially(artistsWithSpotifyId) { case (artist, spotifyId) =>
          wikidataAPI.findBySpotifyId(spotifyId) flatMap { wikidataIds =>
            for {
              _ <- setArtistWikidataId(artist, wikidataIds)
            } yield Thread.sleep(1000)
          }
        }
      } map { _ =>
        Ok
      }
    }
  }
}
