package manggregator.apps

import api.config._
import cats._
import cats.effect._
import cats.implicits._
import library.domain.asset._
import library.domain.page._
import library.domain.chapter.DateReleased
import library.persistence._
import library.resources.database._
import manggregator.Entrypoints
import manggregator.config
import org.legogroup.woof.{_, given}
import java.text.SimpleDateFormat

object SingleCrawl:
  def run(args: List[String]): IO[ExitCode] =
    val format = new SimpleDateFormat("dd/MM/yyyy")
    val minReleaseDate = DateReleased(format.parse(args.tail.head))

    config.load[IO].flatMap { cfg =>
      makeTransactorResource[IO](cfg.database)
        .evalTap(checkSQLiteConnection)
        .use { xa =>
          for
            given Logger[IO] <- Entrypoints.logger()
            storage = Entrypoints.storage(xa)
            libraryServices = Entrypoints.libraryServices(storage)
            library = Entrypoints.library(storage)
            crawling = Entrypoints.crawling()
            _ <- crawling.crawl().run(library)
            assets <- libraryServices.assets
              .findRecentReleases(minReleaseDate)
            _ <- showRecentReleases(assets)
          yield ExitCode.Success
        }
    }

  private def showRecentReleases(assets: List[Asset])(using
      Logger[IO]
  ): IO[Unit] =
    assets match
      case asset :: next =>
        for
          _ <- Logger[IO].info(s"===== ${asset.name} =====")
          _ <- asset.chapters.traverse(chapter =>
            Logger[IO].info(s"${chapter.no} | ${chapter.url}")
          )
        yield showRecentReleases(next)
      case Nil => IO.unit
