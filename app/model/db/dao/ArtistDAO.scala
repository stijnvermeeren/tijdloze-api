package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.ArtistSave
import model.db.dao.table.AllTables

import scala.concurrent.Future

@Singleton
class ArtistDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(artistId: ArtistId): Future[Artist] = {
    db run {
      ArtistTable.filter(_.id === artistId).result.head
    }
  }

  def getAll(): Future[Seq[Artist]] = {
    db run {
      ArtistTable.result
    }
  }

  def create(data: ArtistSave): Future[Artist] = {
    import data._
    val newArtist = Artist(
      firstName = firstName,
      name = name,
      countryId = countryId,
      notes = notes.getOrElse(""),
      urlOfficial = urlOfficial.getOrElse(""),
      urlWikiEn = urlWikiEn.getOrElse(""),
      urlWikiNl = urlWikiNl.getOrElse(""),
      urlAllMusic = urlAllMusic.getOrElse("")
    )

    db run {
      (ArtistTable returning ArtistTable) += newArtist
    }
  }

  def update(artistId: ArtistId, data: ArtistSave): Future[Int] = {
    import data._

    db run {
      ArtistTable
        .filter(_.id === artistId)
        .map(x => (
          x.firstName,
          x.name,
          x.countryId,
          x.notes,
          x.urlOfficial,
          x.urlWikiEn,
          x.urlWikiNl,
          x.urlAllMusic
        )
        )
        .update((
          firstName,
          name,
          countryId,
          notes.getOrElse(""),
          urlOfficial.getOrElse(""),
          urlWikiEn.getOrElse(""),
          urlWikiNl.getOrElse(""),
          urlAllMusic.getOrElse("")
        ))
    }
  }
}
