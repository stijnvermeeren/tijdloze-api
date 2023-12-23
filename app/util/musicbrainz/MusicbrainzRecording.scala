package util.musicbrainz

final case class MusicbrainzRecording(
  id: String,
  title: String,
  artistName: Option[String],
  artistMusicbrainzId: Option[String]
)
