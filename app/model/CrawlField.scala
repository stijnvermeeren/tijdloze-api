package model

import model.db.Artist
import model.db.dao.ArtistDAO
import play.api.libs.json.{JsString, Writes}
import slick.jdbc.H2Profile.MappedColumnType
import slick.jdbc.H2Profile.api.stringColumnType


import scala.concurrent.Future

sealed abstract class CrawlField(
                                  val name: String,
                                  val extractValue: Artist => Option[String],
                                  val save: ArtistDAO => (ArtistId, Option[String]) => Future[Int]
                                ) {
  def equalsExisting(candidateValue: String, newValue: String): Boolean = {
    candidateValue == newValue
  }
}

object CrawlField {
  case object UrlAllMusic extends CrawlField("urlAllMusic", _.urlAllMusic, _.setUrlAllMusic) {
    override def equalsExisting(candidateValue: String, newValue: String): Boolean = {
      def allMusicId(value: String): String = value.split('/').last.split('-').last
      allMusicId(candidateValue) == allMusicId(newValue)
    }
  }

  case object CountryId extends CrawlField("countryId", _.countryId, _.setCountryId)
  case object UrlWikiEn extends CrawlField("urlWikiEn", _.urlWikiEn, _.setUrlWikiEn)
  case object UrlWikiNl extends CrawlField("urlWikiNl", _.urlWikiNl, _.setUrlWikiNl)
  case object UrlOfficial extends CrawlField("urlOfficial", _.urlOfficial, _.setUrlOfficial)
  case object WikidataId extends CrawlField("wikidataId", _.wikidataId, _.setWikidataId)
  case object SpotifyId extends CrawlField("spotifyId", _.spotifyId, _.setSpotifyId)
  case object MusicbrainzId extends CrawlField("musicbrainzId", _.musicbrainzId, _.setMusicbrainzId)

  val allValues = Seq(UrlAllMusic, CountryId, UrlWikiEn, UrlWikiNl, UrlOfficial, WikidataId, SpotifyId, MusicbrainzId)

  implicit val crawlFieldColumnType = MappedColumnType.base[CrawlField, String](
    _.name,
    dbValue => allValues.find(_.name == dbValue).getOrElse(throw new RuntimeException(s"Unknown CrawlField `$dbValue`"))
  )


  implicit val jsonWrites = new Writes[CrawlField] {
    def writes(crawlField: CrawlField) = JsString(crawlField.name)
  }
}
