package manggregator.apps

import cats._
import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits._
import library.domain.asset._
import library.domain.chapter.DateReleased
import library.domain.page._
import library.persistence._
import library.resources.database._
import manggregator.{Entrypoints, config}
import org.legogroup.woof.{_, given}
import ui.core._
import ui.views.MainMenuView

object UI:
  def run(): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
        .use { xa =>
          for
            given Logger[IO] <- Entrypoints.disabledLogger()
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
            view = MainMenuView(
              Context(
                theme,
                services
              )
            )
            _ <- RenderLoop(view).run()
          yield ExitCode.Success
        }
    }
