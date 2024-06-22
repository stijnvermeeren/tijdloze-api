package model.db.dao

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MBDataDAO @Inject()(configProvider: DatabaseConfigProvider) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def get(artistQuery: String, titleQuery: String): Future[String] = {
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
        val minRelevance = results.map(_.relevanceForQuery(titleQuery)).max / 2
        val bestMatch = results
          .filter(_.relevanceForQuery(titleQuery) >= minRelevance)
          .maxBy(result => (-result.releaseYear, result.relevanceForQuery(titleQuery)))
        bestMatch.toString
      } else {
        ""
      }
    }
  }
}

case class MBResult(
                    songMBId: String,
                    matchesAlias: String,
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
    MBResult.searchKey(titleQuery) == MBResult.searchKey(matchesAlias)
  }

  def relevanceForQuery(titleQuery: String): Int = {
    if (isExactMatch(titleQuery) && singleRelationship) {
      if (singleRelationship)
        5 * recordingScore
      else
        recordingScore
    } else {
      recordingScore / 10  // e.g. "Hotellounge (Be the Death of Me)" instead of "Hotellounge"
    }
  }
}

object MBResult {
  def searchKey(value: String, append: String = ""): String = {
    value.toLowerCase.replace("(live)", "").filter(_.isLetterOrDigit) + append
  }
}
