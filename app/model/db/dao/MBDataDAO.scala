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

  private def runQuery(artistQuery: String, titleQuery: String, fuzzyTitleSearch: Boolean = false): Future[Seq[MBResult]] = {
    runArtistQuery(artistQuery) flatMap { artistIds =>
      if (artistIds.nonEmpty) {
        val query = if (fuzzyTitleSearch) {
          sql"""
          SELECT
             mb_song.mb_id as song_mb_id,
             mb_song_alias.alias as matched_alias,
             mb_song.title,
             mb_song.is_single AS single_relationship,
             mb_song.score AS recording_score,
             mb_album.title as album_title,
             mb_album.release_year,
             mb_album.is_main_album,
             mb_album.is_single,
             mb_album.is_soundtrack,
             mb_album.mb_id as album_mb_id,
             mb_artist.name,
             mb_artist.mb_id as artist_mb_id,
             mb_artist.country_id,
             mb_artist2.name as second_artist_name,
             mb_artist2.mb_id as second_artist_mb_id,
             mb_artist2.country_id as second_artist_country_id
          FROM "musicbrainz_export"."mb_song"
          JOIN "musicbrainz_export"."mb_song_alias" ON "mb_song"."id" = "mb_song_alias"."song_id"
          JOIN "musicbrainz_export"."mb_album" ON "mb_album"."id" = "mb_song"."album_id"
          JOIN "musicbrainz_export"."mb_artist" ON "mb_artist"."id" = "mb_song"."artist_id"
          LEFT JOIN "musicbrainz_export"."mb_artist" as "mb_artist2" ON "mb_artist2"."id" = "mb_song"."second_artist_id"
          WHERE ("mb_song_alias"."alias" LIKE ${MBResult.searchKey(titleQuery, "%")}) AND (
            mb_artist.id IN (#${artistIds.mkString(",")})
          )
      """.as[(String, String, String, Boolean, Int, String, Int, Boolean, Boolean, Boolean, String, String, String, String, Option[String], Option[String], Option[String])]
        } else {
          sql"""
          SELECT
             mb_song.mb_id as song_mb_id,
             mb_song_alias.alias as matched_alias,
             mb_song.title,
             mb_song.is_single AS single_relationship,
             mb_song.score AS recording_score,
             mb_album.title as album_title,
             mb_album.release_year,
             mb_album.is_main_album,
             mb_album.is_single,
             mb_album.is_soundtrack,
             mb_album.mb_id as album_mb_id,
             mb_artist.name,
             mb_artist.mb_id as artist_mb_id,
             mb_artist.country_id,
             mb_artist2.name as second_artist_name,
             mb_artist2.mb_id as second_artist_mb_id,
             mb_artist2.country_id as second_artist_country_id
          FROM "musicbrainz_export"."mb_song"
          JOIN "musicbrainz_export"."mb_song_alias" ON "mb_song"."id" = "mb_song_alias"."song_id"
          JOIN "musicbrainz_export"."mb_album" ON "mb_album"."id" = "mb_song"."album_id"
          JOIN "musicbrainz_export"."mb_artist" ON "mb_artist"."id" = "mb_song"."artist_id"
          LEFT JOIN "musicbrainz_export"."mb_artist" as "mb_artist2" ON "mb_artist2"."id" = "mb_song"."second_artist_id"
          WHERE (
            LENGTH("mb_song_alias"."alias") < 255 AND levenshtein_less_equal("mb_song_alias"."alias", ${MBResult.searchKey(titleQuery, "%")}, 1) < 2
          ) AND (
             mb_artist.id IN (#${artistIds.mkString(",")})
          )
      """.as[(String, String, String, Boolean, Int, String, Int, Boolean, Boolean, Boolean, String, String, String, String, Option[String], Option[String], Option[String])]
        }

        db run {
          query
        } map {_.map((MBResult.apply _).tupled)}
      } else {
        Future.successful(Seq.empty)
      }
    }
  }

  private def runArtistQuery(artistQuery: String): Future[Seq[Int]] = {
    val query = sql"""
        SELECT
           mb_artist.id
        FROM "musicbrainz_export"."mb_artist"
        JOIN "musicbrainz_export"."mb_artist_alias" ON "mb_artist"."id" = "mb_artist_alias"."artist_id"
        WHERE
          LENGTH("mb_artist_alias"."alias") < 255
          AND levenshtein_less_equal("mb_artist_alias"."alias", ${MBResult.searchKey(artistQuery)}, 1) < 2
    """.as[Int]

    db run {
      query
    }
  }

  def searchArtistTitle(artistQuery: String, titleQuery: String): Future[Option[MBDatasetHit]] = {
    runQuery(artistQuery, titleQuery) flatMap { results =>
      if (results.nonEmpty) {
        Future.successful(results)
      } else {
        runQuery(artistQuery, titleQuery, fuzzyTitleSearch = true)
      }
    } map { results =>
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
  isMainAlbum: Boolean,
  isSingle: Boolean,
  isSoundtrack: Boolean,
  albumMBId: String,
  name: String,
  artistMBId: String,
  countryId: String,
  secondArtistName: Option[String],
  secondArtistMBId: Option[String],
  secondArtistCountryId: Option[String]
) {
  private def isExactMatch(titleQuery: String): Boolean = {
    MBResult.searchKey(titleQuery) == MBResult.searchKey(matchedAlias)
  }

  def score(titleQuery: String): MBDatasetHit = {
    val isSingleFromFactor = if (singleRelationship) 10 else 1
    val isMainAlbumFactor = if (isMainAlbum) 10 else 1

    // e.g. "Hotellounge (Be the Death of Me)" instead of "Hotellounge"
    val exactMatchFactor = if (isExactMatch(titleQuery)) 10 else 1

    val score = recordingScore.toDouble * isSingleFromFactor * isMainAlbumFactor * exactMatchFactor
    MBDatasetHit(
      songMBId,
      matchedAlias,
      title,
      albumTitle,
      releaseYear,
      isSingle,
      isSoundtrack,
      albumMBId,
      name,
      artistMBId,
      countryId,
      secondArtistName,
      secondArtistMBId,
      secondArtistCountryId,
      score
    )
  }
}

object MBResult {
  def searchKey(value: String, append: String = ""): String = {
    val clean = Normalizer.normalize(value.toLowerCase, Normalizer.Form.NFKD)
    clean.replace("(live)", "").filter(_.isLetterOrDigit) + append
  }
}
