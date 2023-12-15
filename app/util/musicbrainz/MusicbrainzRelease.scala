package util.musicbrainz

final case class MusicbrainzRelease(
  id: String,
  title: String,
  releaseYear: Option[Int],
  releaseGroupId: String
)
