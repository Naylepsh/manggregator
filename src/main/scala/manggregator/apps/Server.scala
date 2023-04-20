package manggregator.apps

import api.config.*
import cats.*
import cats.effect.*
import cats.implicits.*
import library.domain.asset.*
import library.domain.page.*
import library.persistence.*
import library.resources.database.*
import manggregator.{ Entrypoints, config, entrypoints }
import org.legogroup.woof.{ *, given }

object Server:
  given Filter  = Filter.everything
  given Printer = NoColorPrinter()

  def run(): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
        .use { xa =>
          for
            given Logger[IO] <- DefaultLogger.makeIo(entrypoints.Logging.file())
            storage         = Entrypoints.storage(xa)
            library         = Entrypoints.library(storage)
            crawling        = Entrypoints.crawler()
            libraryServices = Entrypoints.libraryServices(storage)
            _ <- Entrypoints
              .http(cfg.apiDocs, library, crawling, libraryServices, cfg.server)
              .useForever
          yield ExitCode.Success
        }
    }
