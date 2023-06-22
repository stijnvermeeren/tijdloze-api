package util.wikidata

case class WikidataDetails(
                            countryId: Seq[String],
                            urlOfficial: Seq[String],
                            urlWikiEn: Seq[String],
                            urlWikiNl: Seq[String],
                            allMusicId: Seq[String],
                            musicbrainzId: Seq[String]
)
