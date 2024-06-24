package model.api

import play.api.libs.json.Json
import util.musicbrainz.{CanonicalMusicbrainzData, MusicbrainzArtist, MusicbrainzRecording, MusicbrainzRelease}


final case class MusicbrainzHit(
  songTitle: String,
  spotifyId: Option[String],
  artistName: String,
  artistCountryId: Option[String],
  artistMusicbrainzId: Option[String],
  albumTitle: String,
  albumReleaseYear: Option[Int],
  albumMusicbrainzId: Option[String],
  albumCover: Option[String]
)

object MusicbrainzHit {
  implicit val jsonWrites = Json.writes[MusicbrainzHit]
}


final case class MusicbrainzResult(
  hit: Option[MusicbrainzHit]
)

object MusicbrainzResult {
  implicit val jsonWrites = Json.writes[MusicbrainzResult]

  def empty: MusicbrainzResult = MusicbrainzResult(hit = None)

  def fromData(
    canonicalData: CanonicalMusicbrainzData,
    musicbrainzRelease: Option[MusicbrainzRelease],
    musicbrainzRecording: Option[MusicbrainzRecording],
    musicbrainzArtist: Option[MusicbrainzArtist],
    cover: Option[String],
    spotifyHit: Option[SpotifyHit]
  ): MusicbrainzResult = {
    def clean(value: String): String = {
      value.replace('’', '\'')
    }
    MusicbrainzResult(
      hit = Some(MusicbrainzHit(
        songTitle = clean(musicbrainzRecording.map(_.title).getOrElse(canonicalData.recordingName)),
        spotifyId = spotifyHit.map(_.spotifyId),
        artistName = clean(musicbrainzRecording.flatMap(_.artistName).getOrElse(canonicalData.artistName)),
        artistCountryId = musicbrainzArtist.flatMap(_.countryId),
        artistMusicbrainzId = musicbrainzRecording.flatMap(_.artistMusicbrainzId),
        albumTitle = clean(musicbrainzRelease.map(_.title).getOrElse(canonicalData.releaseName)),
        albumReleaseYear = musicbrainzRelease.flatMap(_.releaseYear),
        albumMusicbrainzId = musicbrainzRelease.map(_.releaseGroupId),
        albumCover = cover
      ))
    )
  }
}

