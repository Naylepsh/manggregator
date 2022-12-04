package library

import cats.implicits._
import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import library.resources.database._
import library.config.types._

package object persistence:
  private[persistence] def clearAssets(xa: HikariTransactor[IO]) =
    sql"""
    DELETE FROM asset
    """.update.run.void.transact(xa)

  private[persistence] val databaseResource =
    makeTransactorResource[IO](
      DatabaseConfig(
        DatabasePath("./db-test.sqlite"),
        DatabaseUsername("username"),
        DatabasePassword("password")
      )
    )
