package util.musicbrainz

import com.typesafe.config.Config
import model.db.{Album, Artist}
import play.api.libs.json.{JsArray, JsNull, JsNumber, JsString, JsValue}
import play.api.libs.ws.{WSClient, WSRequest}
import util.FutureUtil

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import scala.util.matching.Regex

class MusicbrainzAPI @Inject()(ws: WSClient, config: Config) {
  private def sendSearchRequest(
    endpoint: String,
    id: Option[String] = None,
    query: Seq[(String, String)] = Seq.empty
  ): WSRequest = {
    Thread.sleep(1000)
    ws
      .url(s"http://musicbrainz.org/ws/2/$endpoint/${id.getOrElse("")}")
      .withQueryStringParameters(query:_*)
      .addHttpHeaders(
        "User-Agent" -> s"tijdloze.rocks crawler (https://tijdloze.rocks)",
        "Accept" -> "application/json"
      )
  }

  def searchAsSingle(query: String): Future[Seq[MusicbrainzReleaseGroup]] = {
    val splits = query.split("\\s+")

    def queryFromParts(firstPart: String, secondPart: String): String = {
      s"""(artist:"$firstPart" AND releasegroup:"$secondPart")"""
    }

    val queryParts = for {
      firstPartLength <- 1 until splits.length
      firstPart = splits.take(firstPartLength).mkString(" ")
      secondPart = splits.drop(firstPartLength).mkString(" ")
      queryPart <- Seq(queryFromParts(firstPart, secondPart), queryFromParts(secondPart, firstPart))
    } yield queryPart
    val musicbrainzQuery = s"""(${queryParts.mkString(" OR ")}) AND primarytype:Single"""

    sendSearchRequest("release-group", query = Seq("query" -> musicbrainzQuery)).get().flatMap { response =>
      val items = (response.json \ "release-groups").as[JsArray]

      case class SingleResult(id: String, score: Int, releaseGroup: MusicbrainzReleaseGroup)

      val singleResults = for {
        value <- items.value.toSeq
        id <- (value \ "id").toOption
        score <- (value \ "score").toOption
        releaseGroup <- releaseGroupFromJson(value)
        // TODO reject "Thought I’d Died and Gone to Heaven" as match for "Bryan Adams Heaven"
      } yield SingleResult(
        id = id.as[JsString].value,
        score = score.as[JsNumber].value.toInt,
        releaseGroup = releaseGroup
      )

      val maxScore = singleResults.map(_.score).maxOption
      val maxScoringSingles = singleResults.takeWhile(single => maxScore.contains(single.score))

      FutureUtil.traverseSequentially(maxScoringSingles.map(_.id)) { singleId =>
        sendSearchRequest("release-group", id = Some(singleId), query = Seq("inc" -> "release-group-rels")).get().map { response2 =>
          val relations = (response2.json \ "relations").as[JsArray]

          val datedReleaseGroups = for {
            value <- relations.value
            _ = println(value)
            if (value \ "type").asOpt[String].contains("single from")
            releaseGroupJson <- (value \ "release_group").toOption
            releaseDate <- (releaseGroupJson \ "first-release-date").asOpt[String]
            releaseGroup <- releaseGroupFromJson(releaseGroupJson)
          } yield DatedReleaseGroup(releaseGroup, releaseDate)

          println(datedReleaseGroups)

          datedReleaseGroups.minByOption(_.releaseDate).map(_.releaseGroup)
        }
      }.map(_.flatten)
    }
  }

  def searchAsRecording(query: String): Future[Seq[MusicbrainzReleaseGroup]] = {
    val splits = query.split("\\s+")

    def queryFromParts(firstPart: String, secondPart: String): String = {
      // Proximity search, to allow e.g. matching "Simon Garfunkel" with "Simon & Garfunkel"
      s"""(artist:"$firstPart"~10 AND recording:"$secondPart"~10)"""
    }

    val queryParts = for {
      firstPartLength <- 1 until splits.length
      firstPart = splits.take(firstPartLength).mkString(" ")
      secondPart = splits.drop(firstPartLength).mkString(" ")
      queryPart <- Seq(queryFromParts(firstPart, secondPart), queryFromParts(secondPart, firstPart))
    } yield queryPart
    val musicbrainzQuery = s"""(${queryParts.mkString(" OR ")}) AND primarytype:Album AND status:Official AND isrc:* AND NOT comment:live"""

    sendSearchRequest("recording", query = Seq("query" -> musicbrainzQuery)).get().map { response =>
      val items = (response.json \ "recordings").as[JsArray]

      val releaseGroups = for {
        recording <- items.value
        releases <- (recording \ "releases").toOption.toSeq
        release <- releases.as[JsArray].value
        releaseDate <- (release \ "date").asOpt[String]
        releaseGroup <- (release \ "release-group").toOption
        if (releaseGroup \ "secondary-types").toOption.isEmpty
        releaseGroupCaseClass <- releaseGroupFromJson(releaseGroup)
      } yield DatedReleaseGroup(releaseGroupCaseClass, releaseDate)

      val bestAlbumMatch = releaseGroups
        .filter(_.releaseGroup.releaseType == "Album")
        .groupBy(_.releaseGroup)
        .toSeq
        .sortBy(-_._2.length)
        .headOption
        .map(_._1)
      lazy val bestSingleMatch = releaseGroups
        .filter(_.releaseGroup.releaseType == "Single")
        .groupBy(_.releaseGroup)
        .toSeq
        .sortBy(-_._2.length)
        .headOption
        .map(_._1)

      (bestAlbumMatch orElse bestSingleMatch).toSeq
    }
  }

  def searchAlbum(album: Album, artist: Artist): Future[Seq[MusicbrainzReleaseGroup]] = {
    val artistQuery = artist.musicbrainzId match {
      case Some(musicbrainzId) => s"""arid:"$musicbrainzId""""
      case None => s"""artist:"${artist.name}""""
    }

    val singleSuffix = "(single)"
    val searchTitle = if (album.title.endsWith(singleSuffix)) {
      album.title.dropRight(singleSuffix.length).trim
    } else {
      album.title
    }
    val titleQuery = s"""release:"$searchTitle""""

    val yearQuery = s"""firstreleasedate:${album.releaseYear}"""

    val query = s"$artistQuery AND $titleQuery AND $yearQuery"
    sendSearchRequest("release-group", query = Seq("query" -> query)).get().map { response =>
      val items = (response.json \ "release-groups").as[JsArray]
      val results = items.value.toSeq.flatMap(releaseGroupFromJson)

      val matchingTitle = results.filter(_.title == searchTitle)
      val isAlbum = results.filter(_.releaseType == "Album")
      val matchingTitleAndAlbum = results
        .filter(_.title.toLowerCase == searchTitle.toLowerCase)
        .filter(_.releaseType == "Album")

      if (matchingTitleAndAlbum.length == 1) {
        matchingTitleAndAlbum
      } else if (matchingTitle.length == 1) {
        matchingTitle
      } else if (isAlbum.length == 1) {
        isAlbum
      } else {
        results
      }
    }
  }

  private def releaseGroupFromJson(value: JsValue): Option[MusicbrainzReleaseGroup] = {
    for {
      id <- (value \ "id").toOption
      title <- (value \ "title").toOption
      releaseType <- (value \ "primary-type").toOption
    } yield MusicbrainzReleaseGroup(
      id = id.as[JsString].value,
      title = title.as[JsString].value,
      releaseType = if (releaseType != JsNull) releaseType.as[JsString].value else ""
    )
  }

  def getRecording(id: String): Future[Option[MusicbrainzRecording]] = {
    sendSearchRequest("recording", id = Some(id), query = Seq("inc" -> "artists")).get().map { response =>
      val value = response.json
      for {
        id <- (value \ "id").toOption
        title <- (value \ "title").toOption
        artistMusicbrainzId = (value \ "artist-credit" \ 0 \ "artist" \ "id").toOption
        artistName = (value \ "artist-credit" \ 0 \ "artist" \ "name").toOption
      } yield MusicbrainzRecording(
        id = id.as[JsString].value,
        title = title.as[JsString].value,
        artistName = artistName.map(_.as[JsString].value),
        artistMusicbrainzId = artistMusicbrainzId.map(_.as[JsString].value)
      )
    }
  }

  val wikidataRegex: Regex = """https://www\.wikidata\.org/wiki/(Q[0-9]+)""".r

  def getArtist(id: String): Future[Option[MusicbrainzArtist]] = {
    sendSearchRequest("artist", id = Some(id), query = Seq("inc" -> "url-rels")).get().map { response =>
      val value = response.json
      for {
        id <- (value \ "id").toOption
        name <- (value \ "name").toOption
        countryId = (value \ "area" \ "iso-3166-1-codes" \  0).toOption
        urls = (value \ "relations").toOption.toSeq.flatMap(
          _.as[JsArray].value.flatMap(entry => (entry \ "url" \ "resource").toOption)
        )
      } yield MusicbrainzArtist(
        id = id.as[JsString].value,
        name = name.as[JsString].value,
        countryId = countryId.map(_.as[JsString].value.toLowerCase),
        wikidataId = (urls.map(_.as[JsString].value) flatMap {
          case wikidataRegex(wikidataId) => Some(wikidataId)
          case _ => None
        }).headOption
      )
    }
  }

  def getReleaseGroup(id: String): Future[Option[MusicbrainzReleaseGroup2]] = {
    sendSearchRequest("release-group", id = Some(id), query = Seq("inc" -> "url-rels+artists")).get().map { response =>
      val value = response.json
      for {
        id <- (value \ "id").toOption
        title <- (value \ "title").toOption
        urls = (value \ "relations").toOption.toSeq.flatMap(
          _.as[JsArray].value.flatMap(entry => (entry \ "url" \ "resource").toOption)
        )
        artists = (value \ "artist-credit").toOption.toSeq.flatMap(
          _.as[JsArray].value.flatMap(entry => {
            for {
              name <- (entry \ "artist" \ "name").toOption
              id <- (entry \ "artist" \ "id").toOption
            } yield MusicbrainzArtistAndId(name.as[JsString].value, id.as[JsString].value)
          })
        )
      } yield MusicbrainzReleaseGroup2(
        id = id.as[JsString].value,
        title = title.as[JsString].value,
        wikidataId = (urls.map(_.as[JsString].value) flatMap {
          case wikidataRegex(wikidataId) => Some(wikidataId)
          case _ => None
        }).headOption,
        artists = artists
      )
    }
  }

  def getWork(id: String): Future[Option[MusicbrainzWork]] = {
    sendSearchRequest("work", id = Some(id), query = Seq("inc" -> "url-rels")).get().map { response =>
      val value = response.json
      for {
        id <- (value \ "id").toOption
        title <- (value \ "title").toOption
        urls = (value \ "relations").toOption.toSeq.flatMap(
          _.as[JsArray].value.flatMap(entry => (entry \ "url" \ "resource").toOption)
        )
      } yield MusicbrainzWork(
        id = id.as[JsString].value,
        title = title.as[JsString].value,
        wikidataId = (urls.map(_.as[JsString].value) flatMap {
          case wikidataRegex(wikidataId) => Some(wikidataId)
          case _ => None
        }).headOption
      )
    }
  }
}
