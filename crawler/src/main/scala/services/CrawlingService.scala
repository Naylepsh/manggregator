package crawler.services

import cats.effect._
import cats.effect.std._
import cats.data._
import cats.implicits._
import scala.concurrent.duration._
import scala.language.postfixOps
import crawler.domain.Crawl._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl

object CrawlingService:
  def crawl(): Reader[Library, IO[Unit]] = Reader { library =>
    for {
      resultsQueue <- Queue.bounded[IO, Result](capacity = 10)
      crawler <- Crawler(resultsQueue)
      assetsToCrawl <- library.getAssetsToCrawl()
      jobs = assetsToCrawl.map { case AssetToCrawl(site, assetId, url) =>
        SiteCrawlJob(
          site,
          ScrapeChaptersCrawlJob(url, assetId)
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
    def handle(resultsLeft: Int): IO[Unit] =
      if (resultsLeft > 1)
        for {
          potentialResult <- queue.tryTake
          _ <- potentialResult match {
            case Some(result) =>
              library.handleResult(result) *> handle(resultsLeft - 1)

            case None =>
              IO.sleep(5 seconds) *> handle(resultsLeft)
          }
        } yield ()
      else IO.unit

    handle(resultsToExpect)
