package manggregator.apps

import cats.*
import cats.effect.*
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import library.domain.asset.*
import library.domain.chapter.DateReleased
import library.domain.page.*
import library.persistence.*
import library.resources.database.*
import manggregator.{ Entrypoints, config, entrypoints }
import org.legogroup.woof.{ *, given }
import ui.core.*
import ui.views.MainMenuView

object UI:
  given Filter  = Filter.everything
  given Printer = NoColorPrinter()

  def run(): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      val transactorResource = makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
      val dispatcherResource = Dispatcher.sequential[IO]

      (transactorResource, dispatcherResource).tupled.use {
        case (xa, dispatcher) =>
          for
            given Logger[IO] <- DefaultLogger.makeIo(entrypoints.Logging.file())
            storage         = Entrypoints.storage(xa)
            libraryServices = Entrypoints.libraryServices(storage)
            library         = Entrypoints.library(storage)
            crawler         = Entrypoints.crawler()
            services = Services(
              libraryServices.assets,
              libraryServices.pages,
              libraryServices.chapters,
              crawler,
              library
            )
            theme = Theme.default
            view =
              MainMenuView(
                Context(
                  theme,
                  services,
                  dispatcher
                )
              )
            _ <- RenderLoop(view).run()
          yield ExitCode.Success
      }
    }
