package library.resources

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import library.config.types._

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
