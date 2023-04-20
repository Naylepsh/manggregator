package library.resources

import cats.*
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import library.config.types.*

object database:
  def makeTransactorResource[F[_]: Async](
      config: DatabaseConfig
  ): Resource[F, HikariTransactor[F]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.sqlite.JDBC",
        s"jdbc:sqlite:${config.path.value}",
        config.username.value,
        config.password.value,
        ce
      )
    yield xa

  def checkSQLiteConnection[F[_]: MonadCancelThrow](
      xa: Transactor[F]
  ): F[Unit] =
    sql"SELECT 1".query[Int].unique.transact(xa).void
