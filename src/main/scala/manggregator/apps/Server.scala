package manggregator.apps

import api.config._
import cats._
import cats.effect._
import cats.implicits._
import library.domain.asset._
import library.domain.page._
import library.persistence._
import library.resources.database._
import manggregator.Entrypoints
import manggregator.config
import org.legogroup.woof.{_, given}

object Server:
  def run(): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
        .use { xa =>
          for
            given Logger[IO] <- Entrypoints.logger()
            storage = Entrypoints.storage(xa)
            library = Entrypoints.library(storage)
            crawling = Entrypoints.crawling()
            libraryServices = Entrypoints.libraryServices(storage)
            _ <- Entrypoints
              .http(cfg.apiDocs, library, crawling, libraryServices, cfg.server)
              .useForever
          yield ExitCode.Success
        }
    }
