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

  def create(data: ArtistSave): Future[ArtistId] = {
    import data._
    val newArtist = Artist(
      namePrefix = namePrefix.map(_.trim).filter(_.nonEmpty),
      name = name,
      countryId = countryId,
      notes = notes.map(_.trim).filter(_.nonEmpty),
      urlOfficial = urlOfficial.map(_.trim).filter(_.nonEmpty),
      urlWikiEn = urlWikiEn.map(_.trim).filter(_.nonEmpty),
      urlWikiNl = urlWikiNl.map(_.trim).filter(_.nonEmpty),
      urlAllMusic = urlAllMusic.map(_.trim).filter(_.nonEmpty)
    )

    db run {
      (ArtistTable returning ArtistTable.map(_.id)) += newArtist
    }
  }

  def update(artistId: ArtistId, data: ArtistSave): Future[Int] = {
    import data._

    db run {
      ArtistTable
        .filter(_.id === artistId)
        .map(x => (
          x.namePrefix,
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
          namePrefix.map(_.trim).filter(_.nonEmpty),
          name,
          countryId,
          notes.map(_.trim).filter(_.nonEmpty),
          urlOfficial.map(_.trim).filter(_.nonEmpty),
          urlWikiEn.map(_.trim).filter(_.nonEmpty),
          urlWikiNl.map(_.trim).filter(_.nonEmpty),
          urlAllMusic.map(_.trim).filter(_.nonEmpty)
        ))
    }
  }

  def newArtists(year: Int): Future[Seq[Artist]] = {
    def isNew(artistId: Rep[ArtistId]): Rep[Boolean] = {
      val entryYears = for {
        song <- SongTable.filter(_.artistId === artistId)
        entryYear <- ListEntryTable.filter(_.songId === song.id).map(_.year)
      } yield entryYear

      (entryYears.min === year).ifNull(false)
    }

    db run {
      ArtistTable.filter(artist => isNew(artist.id)).result
    }
  }
}
