package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.AlbumSave
import model.db.dao.table.AlbumTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

@Singleton
class AlbumDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val albumTable = TableQuery[AlbumTable]

  def get(albumId: AlbumId): Future[Album] = {
    db run {
      albumTable.filter(_.id === albumId).result.head
    }
  }

  def getByMusicbrainzId(musicbrainzId: String): Future[Album] = {
    db run {
      albumTable.filter(_.musicbrainzId === musicbrainzId).result.head
    }
  }

  def getAll(): Future[Seq[Album]] = {
    db run {
      albumTable.result
    }
  }

  def create(data: AlbumSave): Future[AlbumId] = {
    import data._
    val newAlbum = Album(
      artistId = artistId,
      title = title,
      releaseYear = releaseYear,
      urlWikiEn = urlWikiEn.map(_.trim).filter(_.nonEmpty),
      urlWikiNl = urlWikiNl.map(_.trim).filter(_.nonEmpty),
      urlAllMusic = urlAllMusic.map(_.trim).filter(_.nonEmpty),
      spotifyId = spotifyId.map(_.trim).filter(_.nonEmpty),
      wikidataId = wikidataId.map(_.trim).filter(_.nonEmpty),
      musicbrainzId = musicbrainzId.map(_.trim).filter(_.nonEmpty),
      cover = cover.map(_.trim).filter(_.nonEmpty),
      isSingle = isSingle,
      isSoundtrack = isSoundtrack
    )

    db run {
      (albumTable returning albumTable.map(_.id)) += newAlbum
    }
  }

  def update(albumId: AlbumId, data: AlbumSave): Future[Int] = {
    import data._

    db run {
      albumTable
        .filter(_.id === albumId)
        .map(x => (
          x.artistId,
          x.title,
          x.releaseYear,
          x.urlWikiEn,
          x.urlWikiNl,
          x.urlAllMusic,
          x.spotifyId,
          x.wikidataId,
          x.musicbrainzId,
          x.cover,
          x.isSingle,
          x.isSoundtrack
        ))
        .update((
          artistId,
          title,
          releaseYear,
          urlWikiEn.map(_.trim).filter(_.nonEmpty),
          urlWikiNl.map(_.trim).filter(_.nonEmpty),
          urlAllMusic.map(_.trim).filter(_.nonEmpty),
          spotifyId.map(_.trim).filter(_.nonEmpty),
          wikidataId.map(_.trim).filter(_.nonEmpty),
          musicbrainzId.map(_.trim).filter(_.nonEmpty),
          cover.map(_.trim).filter(_.nonEmpty),
          isSingle,
          isSoundtrack
        ))
    }
  }

  def delete(albumId: AlbumId): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).delete
    }
  }

  def setUrlWikiEn(albumId: AlbumId, url: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.urlWikiEn).update(url)
    }
  }

  def setUrlWikiNl(albumId: AlbumId, url: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.urlWikiNl).update(url)
    }
  }

  def setUrlAllMusic(albumId: AlbumId, url: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.urlAllMusic).update(url)
    }
  }

  def setSpotifyId(albumId: AlbumId, spotifyId: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.spotifyId).update(spotifyId)
    }
  }

  def setWikidataId(albumId: AlbumId, wikidataId: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.wikidataId).update(wikidataId)
    }
  }

  def setMusicbrainzId(albumId: AlbumId, musicbrainzId: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.musicbrainzId).update(musicbrainzId)
    }
  }

  def setCover(albumId: AlbumId, cover: Option[String]): Future[Int] = {
    db run {
      albumTable.filter(_.id === albumId).map(_.cover).update(cover)
    }
  }
}
