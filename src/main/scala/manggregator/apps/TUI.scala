package manggregator.apps

import cats._
import cats.effect._
import cats.implicits._
import library.domain.asset._
import library.domain.chapter.DateReleased
import library.domain.page._
import library.persistence._
import library.resources.database._
import manggregator.{Entrypoints, config}
import org.legogroup.woof.{_, given}
import tui.resources.MakeTUI

object TUI:
  def run(): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
        .use { xa =>
          for
            given Logger[IO] <- Entrypoints.logger()
            storage = Entrypoints.storage(xa)
            libraryServices = Entrypoints.libraryServices(storage)
            library = Entrypoints.library(storage)
            crawler = Entrypoints.crawler()
            _ <- MakeTUI(libraryServices.assets, crawler, library).make().use {
              _.view()
            }
          yield ExitCode.Success
        }
    }
