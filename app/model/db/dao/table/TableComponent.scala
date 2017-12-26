package model.db.dao.table

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

private[table] trait TableComponent {
  protected val dbConfig: DatabaseConfig[JdbcProfile]
}
