package model.db.dao

import model.api.MBDatasetHit
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.text.Normalizer
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MBDataDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def searchArtistTitle(artistQuery: String, titleQuery: String): Future[Option[MBDatasetHit]] = {
    val query = sql"""
      SELECT
         mb_song.mb_id as song_mb_id,
         mb_song_alias.alias as matched_alias,
         mb_song.title,
         mb_song.is_single AS single_relationship,
         mb_song.score AS recording_score,
         mb_album.title as album_title,
         mb_album.release_year,
         mb_album.is_single,
         mb_album.mb_id as album_mb_id,
         mb_artist.name,
         mb_artist.mb_id as artist_mb_id,
         mb_artist.country_id
      FROM "musicbrainz"."mb_song"
      JOIN "musicbrainz"."mb_song_alias" ON "mb_song"."id" = "mb_song_alias"."song_id"
      JOIN "musicbrainz"."mb_album" ON "mb_album"."id" = "mb_song"."album_id"
      JOIN "musicbrainz"."mb_artist" ON "mb_artist"."id" = "mb_song"."artist_id"
      JOIN "musicbrainz"."mb_artist_alias" ON "mb_artist"."id" = "mb_artist_alias"."artist_id"
      WHERE ("mb_song_alias"."alias" LIKE ${MBResult.searchKey(titleQuery, "%")}) AND (
        LENGTH("mb_artist_alias"."alias") < 255
        AND levenshtein_less_equal("mb_artist_alias"."alias", LOWER(REGEXP_REPLACE(${MBResult.searchKey(artistQuery)}, '\\W', '', 'g')), 1) < 2
      )
  """.as[(String, String, String, Boolean, Int, String, Int, Boolean, String, String, String, String)]
    db run {
      query
    } map {_.map((MBResult.apply _).tupled)} map { results =>
      if (results.nonEmpty) {
        val scoredResults = results.map(_.score(titleQuery))
        val minRelevance = scoredResults.map(_.score).max / 2
        val bestMatch = scoredResults
          .filter(_.score >= minRelevance)
          .maxBy(result => (-result.releaseYear, result.score))
        Some(bestMatch)
      } else {
        None
      }
    }
  }

  def searchQuery(query: String): Future[Option[MBDatasetHit]] = {
    val parts = query.split("\\s+").filter(_.nonEmpty)
    Future.traverse((1 until parts.length).toList) { i =>
      // TODO also try title first?
      val artistQuery = parts.take(i).mkString(" ")
      val titleQuery = parts.drop(i).mkString(" ")
      searchArtistTitle(artistQuery, titleQuery)
    } map { allResults =>
      allResults.flatten.maxByOption(_.score)
    }
  }
}

case class MBResult(
                    songMBId: String,
                    matchedAlias: String,
                    title: String,
                    singleRelationship: Boolean,
                    recordingScore: Int,
                    albumTitle: String,
                    releaseYear: Int,
                    isSingle: Boolean,
                    albumMBId: String,
                    name: String,
                    artistMBId: String,
                    countryId: String
) {
  private def isExactMatch(titleQuery: String): Boolean = {
    MBResult.searchKey(titleQuery) == MBResult.searchKey(matchedAlias)
  }

  def score(titleQuery: String): MBDatasetHit = {
    val score = if (isExactMatch(titleQuery) && singleRelationship) {
      if (singleRelationship)
        5 * recordingScore
      else
        recordingScore
    } else {
      recordingScore.toDouble / 10  // e.g. "Hotellounge (Be the Death of Me)" instead of "Hotellounge"
    }

    MBDatasetHit(
      songMBId, matchedAlias, title, albumTitle, releaseYear, isSingle, albumMBId, name, artistMBId, countryId, score
    )
  }
}

object MBResult {
  def searchKey(value: String, append: String = ""): String = {
    val clean = Normalizer.normalize(value.toLowerCase, Normalizer.Form.NFKD)
    clean.replace("(live)", "").filter(_.isLetterOrDigit) + append
  }
}
