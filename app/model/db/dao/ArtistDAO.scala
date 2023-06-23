package model
package db
package dao

import javax.inject.{Inject, Singleton}
import model.api.ArtistSave
import model.db.dao.table.ArtistTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

@Singleton
class ArtistDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val artistTable = TableQuery[ArtistTable]

  def get(artistId: ArtistId): Future[Artist] = {
    db run {
      artistTable.filter(_.id === artistId).result.head
    }
  }

  def getAll(): Future[Seq[Artist]] = {
    db run {
      artistTable.result
    }
  }

  def create(data: ArtistSave): Future[ArtistId] = {
    import data._
    val newArtist = Artist(
      namePrefix = namePrefix.map(_.trim).filter(_.nonEmpty),
      name = name,
      aliases = aliases.map(_.trim).filter(_.nonEmpty),
      countryId = countryId.map(_.trim).filter(_.nonEmpty),
      notes = notes.map(_.trim).filter(_.nonEmpty),
      urlOfficial = urlOfficial.map(_.trim).filter(_.nonEmpty),
      urlWikiEn = urlWikiEn.map(_.trim).filter(_.nonEmpty),
      urlWikiNl = urlWikiNl.map(_.trim).filter(_.nonEmpty),
      urlAllMusic = urlAllMusic.map(_.trim).filter(_.nonEmpty),
      spotifyId = spotifyId.map(_.trim).filter(_.nonEmpty),
      wikidataId = wikidataId.map(_.trim).filter(_.nonEmpty),
      musicbrainzId = musicbrainzId.map(_.trim).filter(_.nonEmpty)
    )

    db run {
      (artistTable returning artistTable.map(_.id)) += newArtist
    }
  }

  def update(artistId: ArtistId, data: ArtistSave): Future[Int] = {
    import data._

    db run {
      artistTable
        .filter(_.id === artistId)
        .map(x => (
          x.namePrefix,
          x.name,
          x.aliases,
          x.countryId,
          x.notes,
          x.urlOfficial,
          x.urlWikiEn,
          x.urlWikiNl,
          x.urlAllMusic,
          x.spotifyId,
          x.wikidataId,
          x.musicbrainzId
        )
        )
        .update((
          namePrefix.map(_.trim).filter(_.nonEmpty),
          name,
          aliases.map(_.trim).filter(_.nonEmpty),
          countryId.map(_.trim).filter(_.nonEmpty),
          notes.map(_.trim).filter(_.nonEmpty),
          urlOfficial.map(_.trim).filter(_.nonEmpty),
          urlWikiEn.map(_.trim).filter(_.nonEmpty),
          urlWikiNl.map(_.trim).filter(_.nonEmpty),
          urlAllMusic.map(_.trim).filter(_.nonEmpty),
          spotifyId.map(_.trim).filter(_.nonEmpty),
          wikidataId.map(_.trim).filter(_.nonEmpty),
          musicbrainzId.map(_.trim).filter(_.nonEmpty)
        ))
    }
  }

  def delete(artistId: ArtistId): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).delete
    }
  }

  def setCountryId(artistId: ArtistId, countryId: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.countryId).update(countryId)
    }
  }

  def setSpotifyId(artistId: ArtistId, spotifyId: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.spotifyId).update(spotifyId)
    }
  }

  def setWikidataId(artistId: ArtistId, wikidataId: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.wikidataId).update(wikidataId)
    }
  }

  def setMusicbrainzId(artistId: ArtistId, wikidataId: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.musicbrainzId).update(wikidataId)
    }
  }

  def setUrlWikiEn(artistId: ArtistId, url: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.urlWikiEn).update(url)
    }
  }

  def setUrlWikiNl(artistId: ArtistId, url: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.urlWikiNl).update(url)
    }
  }

  def setUrlOfficial(artistId: ArtistId, url: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.urlOfficial).update(url)
    }
  }

  def setUrlAllMusic(artistId: ArtistId, url: Option[String]): Future[Int] = {
    db run {
      artistTable.filter(_.id === artistId).map(_.urlAllMusic).update(url)
    }
  }
}
