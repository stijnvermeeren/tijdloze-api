package util.musicbrainz

final case class MusicbrainzArtist(
  id: String,
  name: String,
  countryId: Option[String]
)
