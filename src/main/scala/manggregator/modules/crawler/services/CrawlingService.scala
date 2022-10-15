package manggregator.modules.crawler.services

import cats.effect._
import cats.effect.std._
import cats.data._
import cats.implicits._
import scala.concurrent.duration._
import scala.language.postfixOps
import manggregator.modules.crawler.domain.Crawl._
import manggregator.modules.crawler.domain.Crawl.CrawlResult._
import manggregator.modules.crawler.domain.Crawl.CrawlJob._
import manggregator.modules.crawler.domain.Library
import manggregator.modules.crawler.domain.Library.AssetToCrawl

object CrawlingService:
  def crawl(): Reader[Library, IO[Unit]] = Reader { library =>
    for {
      resultsQueue <- Queue.bounded[IO, Result](capacity = 10)
      crawler <- Crawler(resultsQueue)
      assetsToCrawl <- library.getAssetsToCrawl()
      jobs = assetsToCrawl.map { case AssetToCrawl(site, assetTitle, url) =>
        SiteCrawlJob(
          site,
          ScrapeChaptersCrawlJob(url, assetTitle)
        )
      }
      _ <- crawler.enqueue(jobs)
      _ <- (
        crawler.crawl(),
        handleResults(resultsQueue, library, assetsToCrawl.length)
      ).parTupled.void
    } yield ()
  }

  private def handleResults(
      queue: Queue[IO, Result],
      library: Library,
      resultsToExpect: Int
  ): IO[Unit] =
    if (resultsToExpect > 1)
      for {
        potentialResult <- queue.tryTake
        _ <- potentialResult match {
          case Some(result) =>
            library.handleResult(result) *> handleResults(
              queue,
              library,
              resultsToExpect - 1
            )

          case None =>
            IO.sleep(5 seconds) *> handleResults(
              queue,
              library,
              resultsToExpect
            )
        }
      } yield ()
    else IO.unit
