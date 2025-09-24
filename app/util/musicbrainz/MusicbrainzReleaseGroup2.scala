package util.musicbrainz

final case class MusicbrainzReleaseGroup2(
  id: String,
  title: String,
  wikidataId: Option[String],
  artists: Seq[MusicbrainzArtistAndId]
)

final case class MusicbrainzArtistAndId(
  name: String,
  id: String
)
