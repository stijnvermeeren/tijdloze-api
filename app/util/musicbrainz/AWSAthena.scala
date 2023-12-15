package util.musicbrainz

import com.typesafe.config.Config
import model.db.{Album, Artist}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest}
import _root_.util.FutureUtil
import io.burt.athena.AthenaDataSource

import java.sql.DriverManager
import java.util.Properties
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AWSAthena @Inject()(ws: WSClient, config: Config) {

  def search(input: String): Seq[Row] = {
    val dataSource = new AthenaDataSource()
    dataSource.setWorkGroup("primary")
    dataSource.setRegion("eu-central-1")
    dataSource.setDatabase("tijdlozemusicbrainz") // Due to a bug in io.burt.athena, this cannot contain hyphens!
    dataSource.setOutputLocation("s3://tijdloze-musicbrainz/athena-output/")
    val connection = dataSource.getConnection()
    val statement = connection.createStatement

    val splits = input.split("\\s+")

    def sanitize(value: String): String = {
      s"'${value.replace("'", "''")}'"
    }

    def normalize(value: String): String = {
      value.toLowerCase.replaceAll("[^a-zA-Z0-9 ]", "")
    }

    def queryFromParts(firstPart: String, secondPart: String): String = {
      def compare(field: String, value: String): String = {
        // TODO safer SQL query building
        val valuePart = sanitize(normalize(value))
        val query = Seq(
          s"""position($valuePart IN $field) > 0""",
          s"""levenshtein_distance($valuePart, $field) < ${value.length / 4}"""
        ).mkString(" OR ")
        s"($query)"
      }

      val query = Seq(
        compare("norm_title", firstPart),
        compare("norm_artist", secondPart)
      ).mkString(" AND ")
      s"($query)"
    }

    val queryParts = for {
      firstPartLength <- 1 until splits.length
      firstPart = splits.take(firstPartLength).mkString(" ")
      secondPart = splits.drop(firstPartLength).mkString(" ")
      queryPart <- Seq(queryFromParts(firstPart, secondPart), queryFromParts(secondPart, firstPart))
    } yield queryPart
    val where = queryParts.mkString(" OR ")
    val filterWords = Set("the", "of", "in", "to", "for", "a", "an", "on")
    val inputArray = s"ARRAY[${
      splits.map(_.toLowerCase)
        .filterNot(filterWords.contains)
        .map(value => sanitize(normalize(value)))
        .mkString(", ")
    }]"
    val query = Seq(
      "WITH candidates AS (",
      Seq(
        "SELECT",
        "recording_mbid, norm_artist, norm_title, score, artist_credit_name, release_name, release_mbid, year, recording_name, ",
        s"split(norm_title, ' ') || split(norm_artist, ' ') AS match_words",
        "FROM parquet",
        s"WHERE arrays_overlap($inputArray, split(norm_artist, ' ')) OR arrays_overlap($inputArray, split(norm_title, ' '))"
      ).mkString(" "),
      "),",
      "computed AS (",
      Seq(
        "SELECT *,",
        s"cast(cardinality(array_intersect($inputArray, match_words)) as double) AS intersection,",
        s"cardinality(match_words) AS match_word_count",
        "FROM candidates"
      ).mkString(" "),
      ")",
      "SELECT *",
      "FROM computed",
      s"WHERE $where",
      s"ORDER BY intersection / ${inputArray.length} DESC, score ASC",
      "LIMIT 10"
    ).mkString(" ")
    try {
      val resultSet = statement.executeQuery(query)

      val results = scala.collection.mutable.Buffer.empty[Row]
      while (resultSet.next()) {
        results.append(Row(
          artistName = resultSet.getString("artist_credit_name"),
          releaseName = resultSet.getString("release_name"),
          releaseId = resultSet.getString("release_mbid"),
          recordingName = resultSet.getString("recording_name"),
          recordingId = resultSet.getString("recording_mbid"),
          score = resultSet.getInt("score"),
          year = resultSet.getString("year")
        ))
      }
      connection.close()

      results.toSeq
    } catch {
      case e: Exception =>
        println(e)
        Seq.empty
    }
  }
}

final case class Row(artistName: String,
                     releaseId: String,
                     releaseName: String,
                     recordingId: String,
                     recordingName: String,
                     score: Int,
                     year: String)
