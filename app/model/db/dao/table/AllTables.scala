package model.db.dao.table

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

@Singleton
private[dao] class AllTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends AlbumTableComponent
  with ArtistTableComponent
  with SongTableComponent
  with CommentTableComponent
  with UserTableComponent
  with ListEntryTableComponent
  with LogUserDisplayNameTableComponent
{
  val dbConfig = dbConfigProvider.get[JdbcProfile]
}
