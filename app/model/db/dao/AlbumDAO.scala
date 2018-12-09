package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.AlbumSave
import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class AlbumDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(albumId: AlbumId): Future[Album] = {
    db run {
      AlbumTable.filter(_.id === albumId).result.head
    }
  }

  def getAll(): Future[Seq[Album]] = {
    db run {
      AlbumTable.result
    }
  }

  def create(data: AlbumSave): Future[AlbumId] = {
    import data._
    val newAlbum = Album(
      artistId = artistId,
      title = title,
      releaseYear = releaseYear,
      urlWikiEn = urlWikiEn.getOrElse(""),
      urlWikiNl = urlWikiNl.getOrElse(""),
      urlAllMusic = urlAllMusic.getOrElse("")
    )

    db run {
      (AlbumTable returning AlbumTable.map(_.id)) += newAlbum
    }
  }

  def update(albumId: AlbumId, data: AlbumSave): Future[Int] = {
    import data._

    db run {
      AlbumTable
        .filter(_.id === albumId)
        .map(x => (
          x.artistId,
          x.title,
          x.releaseYear,
          x.urlWikiEn,
          x.urlWikiNl,
          x.urlAllMusic
        )
        )
        .update((
          artistId,
          title,
          releaseYear,
          urlWikiEn.getOrElse(""),
          urlWikiNl.getOrElse(""),
          urlAllMusic.getOrElse("")
        ))
    }
  }
}
