package library.suite

import cats.effect.*
import cats.implicits.*
import doobie.implicits.*
import weaver.{ Expectations, IOSuite }
import doobie.hikari.HikariTransactor
import library.resources.database.*
import library.config.types.*
import doobie.util.fragment.Fragment

abstract class DatabaseSuite extends ResourceSuite:
  override def maxParallelism = 1

  override type Res = HikariTransactor[IO]
  override def sharedResource: Resource[cats.effect.IO, Res] =
    databaseResource

  def testWithCleanDb(name: String)(fa: Res => IO[Expectations]): Unit =
    testBeforeEach(clearTables)(name)(fa)

  private def clearTables(xa: HikariTransactor[IO]): IO[Unit] =
    for
      tables <- getTableNames(xa)
      _ <- tables
        .filter(_ != "schema_migrations")
        .foldLeft(IO.unit)((acc, table) => acc >> clearTable(xa, table))
    yield ()

  private def getTableNames(xa: HikariTransactor[IO]): IO[List[String]] =
    sql"SELECT tbl_name FROM sqlite_master where type='table'"
      .query[String]
      .to[List]
      .transact(xa)

  private def clearTable(
      xa: HikariTransactor[IO],
      tableName: String
  ): IO[Unit] =
    Fragment.const(s"DELETE FROM $tableName").update.run.void.transact(xa)

  private val databaseResource =
    makeTransactorResource[IO](
      DatabaseConfig(
        DatabasePath("./db-test.sqlite"),
        DatabaseUsername("username"),
        DatabasePassword("password")
      )
    )
