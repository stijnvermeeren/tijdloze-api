package model
package db
package dao
package table

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

private[table] trait SongTableComponent extends TableComponent {
  import dbConfig.profile.api._

  class SongTable(tag: Tag) extends Table[Song](tag, "nummer") {
    val id = column[SongId]("id", O.AutoInc, O.PrimaryKey)
    val artistId = column[ArtistId]("artiest_id")
    val albumId = column[AlbumId]("album_id")
    val title = column[String]("titel")
    val lyrics = column[String]("lyrics")
    val languageId = column[String]("taal_afkorting")
    val leadVocals = column[String]("lead_vocals")
    val notes = column[String]("opmerkingen")
    val urlWikiEn = column[String]("url_wikien")
    val urlWikiNl = column[String]("url_wikinl")
    val edit = column[Boolean]("edit")
    val lastUpdate = column[DateTime]("last_update")

    val j87 = column[Int]("j87")
    val j88 = column[Int]("j88")
    val j90 = column[Int]("j90")
    val j91 = column[Int]("j91")
    val j92 = column[Int]("j92")
    val j93 = column[Int]("j93")
    val j94 = column[Int]("j94")
    val j95 = column[Int]("j95")
    val j96 = column[Int]("j96")
    val j97 = column[Int]("j97")
    val j98 = column[Int]("j98")
    val j99 = column[Int]("j99")
    val j00 = column[Int]("j00")
    val j01 = column[Int]("j01")
    val j02 = column[Int]("j02")
    val j03 = column[Int]("j03")
    val j04 = column[Int]("j04")
    val j05 = column[Int]("j05")
    val j06 = column[Int]("j06")
    val j07 = column[Int]("j07")
    val j08 = column[Int]("j08")
    val j09 = column[Int]("j09")
    val j10 = column[Int]("j10")
    val j11 = column[Int]("j11")
    val j12 = column[Int]("j12")
    val j13 = column[Int]("j13")
    val j14 = column[Int]("j14")
    val j15 = column[Int]("j15")
    val j16 = column[Int]("j16")
    val j17 = column[Int]("j17")
    val exitCurrent = column[Boolean]("exit_huidige")

    type RowType = SongId :: ArtistId :: AlbumId :: String :: String :: String :: String :: String :: String :: String :: Boolean :: DateTime ::
      Int :: Int :: Int :: Int :: Int ::
      Int :: Int :: Int :: Int :: Int ::
      Int :: Int :: Int :: Int :: Int ::
      Int :: Int :: Int :: Int :: Int ::
      Int :: Int :: Int :: Int :: Int ::
      Int :: Int :: Int :: Int :: Int ::
      Boolean :: HNil

    def * = (
      id :: artistId :: albumId :: title :: lyrics :: languageId :: leadVocals :: notes :: urlWikiEn :: urlWikiNl :: edit :: lastUpdate ::
        j87 :: j88 :: j90 :: j91 :: j92 ::
        j93 :: j94 :: j95 :: j96 :: j97 ::
        j98 :: j99 :: j00 :: j01 :: j02 ::
        j03 :: j04 :: j05 :: j06 :: j07 ::
        j08 :: j09 :: j10 :: j11 :: j12 ::
        j13 :: j14 :: j15 :: j16 :: j17 ::
        exitCurrent :: HNil
      ) <> (createSong, extractFromSong)

    def createSong(row: RowType): Song = {
      row match {
        case id :: artistId :: albumId :: title :: lyrics :: languageId :: leadVocals :: notes :: urlWikiEn :: urlWikiNl :: edit :: lastUpdate ::
          j87 :: j88 :: j90 :: j91 :: j92 ::
          j93 :: j94 :: j95 :: j96 :: j97 ::
          j98 :: j99 :: j00 :: j01 :: j02 ::
          j03 :: j04 :: j05 :: j06 :: j07 ::
          j08 :: j09 :: j10 :: j11 :: j12 ::
          j13 :: j14 :: j15 :: j16 :: j17 ::
          exitCurrent :: HNil =>

          val positions = Map(
            "87" -> j87,
            "88" -> j88,
            "90" -> j90,
            "91" -> j91,
            "92" -> j92,
            "93" -> j93,
            "94" -> j94,
            "95" -> j95,
            "96" -> j96,
            "97" -> j97,
            "98" -> j98,
            "99" -> j99,
            "00" -> j00,
            "01" -> j01,
            "02" -> j02,
            "03" -> j03,
            "04" -> j04,
            "05" -> j05,
            "06" -> j06,
            "07" -> j07,
            "08" -> j08,
            "09" -> j09,
            "10" -> j10,
            "11" -> j11,
            "12" -> j12,
            "13" -> j13,
            "14" -> j14,
            "15" -> j15,
            "16" -> j16,
            "17" -> j17
          )
          Song(id, artistId, albumId, title, positions, exitCurrent, lyrics, languageId, leadVocals, notes, urlWikiEn, urlWikiNl, edit, lastUpdate)
      }
    }

    def extractFromSong(song: Song): Option[RowType] = {
      song match {
        case Song(id, artistId, albumId, title, positions, exitCurrent, lyrics, languageId, leadVocals, notes, urlWikiEn, urlWikiNl, edit, lastUpdate) =>
          Some(
            id :: artistId :: albumId :: title :: lyrics :: languageId :: leadVocals :: notes :: urlWikiEn :: urlWikiNl :: edit :: lastUpdate ::
              positions("87") :: positions("88") :: positions("90") :: positions("91") :: positions("92") ::
              positions("93") :: positions("94") :: positions("95") :: positions("96") :: positions("97") ::
              positions("98") :: positions("99") :: positions("00") :: positions("01") :: positions("02") ::
              positions("03") :: positions("04") :: positions("05") :: positions("06") :: positions("07") ::
              positions("08") :: positions("09") :: positions("10") :: positions("11") :: positions("12") ::
              positions("13") :: positions("14") :: positions("15") :: positions("16") :: positions("17") ::
              exitCurrent :: HNil
          )
      }
    }
  }

  val SongTable = TableQuery[SongTable]
}
