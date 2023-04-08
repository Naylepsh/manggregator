package manggregator.apps

import api.config._
import cats._
import cats.effect._
import cats.implicits._
import library.domain.asset._
import library.domain.page._
import library.persistence._
import library.resources.database._
import manggregator.{Entrypoints, config, entrypoints}
import org.legogroup.woof.{_, given}

object Server:
  given Filter = Filter.everything
  given Printer = NoColorPrinter()

  def run(): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
        .use { xa =>
          for
            given Logger[IO] <- DefaultLogger.makeIo(entrypoints.Logging.file())
            storage = Entrypoints.storage(xa)
            library = Entrypoints.library(storage)
            crawling = Entrypoints.crawler()
            libraryServices = Entrypoints.libraryServices(storage)
            _ <- Entrypoints
              .http(cfg.apiDocs, library, crawling, libraryServices, cfg.server)
              .useForever
          yield ExitCode.Success
        }
    }
