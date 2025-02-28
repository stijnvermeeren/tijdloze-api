package model

import model.db.{Album, Artist, Song}
import model.db.dao.{AlbumDAO, ArtistDAO, SongDAO}
import play.api.libs.json.{JsString, Writes}
import slick.jdbc.H2Profile.MappedColumnType
import slick.jdbc.H2Profile.api.stringColumnType

import scala.concurrent.Future

trait CrawlField[Model, Id, Table] {
  val name: String
  val extractValue: Model => Option[String]
  val save: Table => (Id, Option[String]) => Future[Int]

  def equalsExisting(candidateValue: String, newValue: String): Boolean = {
    candidateValue == newValue
  }
}

sealed abstract class ArtistCrawlField(
  val name: String,
  val extractValue: Artist => Option[String],
  val save: ArtistDAO => (ArtistId, Option[String]) => Future[Int]
) extends CrawlField[Artist, ArtistId, ArtistDAO]

object ArtistCrawlField {
  case object UrlAllMusic extends ArtistCrawlField("urlAllMusic", _.urlAllMusic, _.setUrlAllMusic) {
    override def equalsExisting(candidateValue: String, newValue: String): Boolean = {
      def allMusicId(value: String): String = value.split('/').last.split('-').last
      allMusicId(candidateValue) == allMusicId(newValue)
    }
  }

  case object CountryId extends ArtistCrawlField("countryId", _.countryId, _.setCountryId)
  case object UrlWikiEn extends ArtistCrawlField("urlWikiEn", _.urlWikiEn, _.setUrlWikiEn)
  case object UrlWikiNl extends ArtistCrawlField("urlWikiNl", _.urlWikiNl, _.setUrlWikiNl)
  case object UrlOfficial extends ArtistCrawlField("urlOfficial", _.urlOfficial, _.setUrlOfficial)
  case object WikidataId extends ArtistCrawlField("wikidataId", _.wikidataId, _.setWikidataId)
  case object SpotifyId extends ArtistCrawlField("spotifyId", _.spotifyId, _.setSpotifyId)
  case object MusicbrainzId extends ArtistCrawlField("musicbrainzId", _.musicbrainzId, _.setMusicbrainzId)

  val allValues = Seq(UrlAllMusic, CountryId, UrlWikiEn, UrlWikiNl, UrlOfficial, WikidataId, SpotifyId, MusicbrainzId)

  implicit val crawlFieldColumnType = MappedColumnType.base[ArtistCrawlField, String](
    _.name,
    dbValue => allValues.find(_.name == dbValue).getOrElse(throw new RuntimeException(s"Unknown ArtistCrawlField `$dbValue`"))
  )


  implicit val jsonWrites = new Writes[ArtistCrawlField] {
    def writes(crawlField: ArtistCrawlField) = JsString(crawlField.name)
  }
}

sealed abstract class AlbumCrawlField(
  val name: String,
  val extractValue: Album => Option[String],
  val save: AlbumDAO => (AlbumId, Option[String]) => Future[Int]
) extends CrawlField[Album, AlbumId, AlbumDAO]

object AlbumCrawlField {
  case object UrlAllMusic extends AlbumCrawlField("urlAllMusic", _.urlAllMusic, _.setUrlAllMusic) {
    override def equalsExisting(candidateValue: String, newValue: String): Boolean = {
      def allMusicId(value: String): String = value.split('/').last.split('-').last
      allMusicId(candidateValue) == allMusicId(newValue)
    }
  }

  case object UrlWikiEn extends AlbumCrawlField("urlWikiEn", _.urlWikiEn, _.setUrlWikiEn)
  case object UrlWikiNl extends AlbumCrawlField("urlWikiNl", _.urlWikiNl, _.setUrlWikiNl)
  case object WikidataId extends AlbumCrawlField("wikidataId", _.wikidataId, _.setWikidataId)
  case object SpotifyId extends AlbumCrawlField("spotifyId", _.spotifyId, _.setSpotifyId)
  case object MusicbrainzId extends AlbumCrawlField("musicbrainzId", _.musicbrainzId, _.setMusicbrainzId)
  case object Cover extends AlbumCrawlField("cover", _.cover, _.setCover)

  val allValues = Seq(UrlAllMusic, UrlWikiEn, UrlWikiNl, WikidataId, SpotifyId, MusicbrainzId, Cover)

  implicit val crawlFieldColumnType = MappedColumnType.base[AlbumCrawlField, String](
    _.name,
    dbValue => allValues.find(_.name == dbValue).getOrElse(throw new RuntimeException(s"Unknown AlbumCrawlField `$dbValue`"))
  )

  implicit val jsonWrites = new Writes[AlbumCrawlField] {
    def writes(crawlField: AlbumCrawlField) = JsString(crawlField.name)
  }
}


sealed abstract class SongCrawlField(
  val name: String,
  val extractValue: Song => Option[String],
  val save: SongDAO => (SongId, Option[String]) => Future[Int]
) extends CrawlField[Song, SongId, SongDAO]

object SongCrawlField {
  // TODO clear cache and update currentList websocket
  case object LanguageId extends SongCrawlField("languageId", _.languageId, _.setLanguageId)

  case object UrlWikiEn extends SongCrawlField("urlWikiEn", _.urlWikiEn, _.setUrlWikiEn)
  case object UrlWikiNl extends SongCrawlField("urlWikiNl", _.urlWikiNl, _.setUrlWikiNl)
  case object WikidataId extends SongCrawlField("wikidataId", _.wikidataId, _.setWikidataId)
  case object SpotifyId extends SongCrawlField("spotifyId", _.spotifyId, _.setSpotifyId)
  case object MusicbrainzRecordingId extends SongCrawlField("musicbrainzRecordingId", _.musicbrainzRecordingId, _.setMusicbrainzRecordingId)
  case object MusicbrainzWorkId extends SongCrawlField("musicbrainzWorkId", _.musicbrainzWorkId, _.setMusicbrainzWorkId)

  val allValues = Seq(LanguageId, UrlWikiEn, UrlWikiNl, WikidataId, SpotifyId, MusicbrainzRecordingId, MusicbrainzWorkId)

  implicit val crawlFieldColumnType = MappedColumnType.base[SongCrawlField, String](
    _.name,
    dbValue => allValues.find(_.name == dbValue).getOrElse(throw new RuntimeException(s"Unknown SongCrawlField `$dbValue`"))
  )

  implicit val jsonWrites = new Writes[SongCrawlField] {
    def writes(crawlField: SongCrawlField) = JsString(crawlField.name)
  }
}
