package util.musicbrainz

final case class MusicbrainzWork(
  id: String,
  title: String,
  wikidataId: Option[String]
)
