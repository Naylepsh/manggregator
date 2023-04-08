package manggregator.apps

import cats._
import cats.effect._
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import cats.implicits._
import library.domain.asset._
import library.domain.chapter.DateReleased
import library.domain.page._
import library.persistence._
import library.resources.database._
import manggregator.{Entrypoints, config, entrypoints}
import org.legogroup.woof.{_, given}
import ui.core._
import ui.views.MainMenuView

object UI:
  given Filter = Filter.everything
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
            storage = Entrypoints.storage(xa)
            libraryServices = Entrypoints.libraryServices(storage)
            library = Entrypoints.library(storage)
            crawler = Entrypoints.crawler()
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
