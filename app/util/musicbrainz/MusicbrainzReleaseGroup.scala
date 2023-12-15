package util.musicbrainz

final case class MusicbrainzReleaseGroup(
  id: String,
  title: String,
  releaseType: String
)

case class DatedReleaseGroup(releaseGroup: MusicbrainzReleaseGroup, releaseDate: String)