package util.crawl

import model.db.Artist
import model.{ArtistId, CrawlField}
import model.db.dao.{ArtistDAO, CrawlArtistDAO}
import play.api.cache.AsyncCacheApi
import util.FutureUtil

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CrawlHelper @Inject()(crawlArtistDAO: CrawlArtistDAO, artistDAO: ArtistDAO, cache: AsyncCacheApi) {
  def process(artist: Artist,
              field: CrawlField,
              candidateValues: Seq[String],
              comment: String,
              strategy: Strategy) = {

    val situation = if (candidateValues.length == 1) UniqueValue else NonUniqueValue

    FutureUtil.traverseSequentially(candidateValues) { candidateValue =>
      val compareWithExisting = field.extractValue(artist) match {
        case Some(value) if field.equalsExisting(candidateValue, value) => EqualsExistingValue
        case Some(_) => NotEqualsExistingValue
        case None => NoExistingValue
      }

      strategy.decide(situation, compareWithExisting) match {
        case AutoAccept =>
          crawlArtistDAO.saveAuto(
            artist.id,
            field = field,
            value = Some(candidateValue),
            comment = Some(comment),
            isAccepted = true
          ) flatMap { _ =>
            field.save(artistDAO)(artist.id, Some(candidateValue))
          } map { _ =>
            cache.remove(s"artist/${artist.id.value}")
          }
        case Pending =>
          crawlArtistDAO.savePending(
            artist.id,
            field = field,
            value = Some(candidateValue),
            comment = Some(comment)
          )
      }
    }
  }
}
