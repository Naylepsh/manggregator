package manggregator.modules.crawler.services

import cats.effect._
import cats.effect.std._
import manggregator.modules.crawler.domain.Crawl._
import manggregator.modules.crawler.domain.Crawl.CrawlResult._
import manggregator.modules.crawler.domain.Crawl.CrawlJob._
import manggregator.modules.crawler.domain.Library
import manggregator.modules.shared.domain.ChapterCrawl

class CrawlingService(val library: Library):
  /** WARNING: The queue handling is currently synchronous. First the stuff is
    * being put on result queue until crawler.queue finishes. Once that process
    * finishes, this service's results queue handling takes effect.
    *
    * TODO: Make handling of these asynchronous
    */
  def crawl(): IO[Unit] = for {
    resultsQueue <- Queue.bounded[IO, Result](capacity = 10)
    crawler <- Crawler(resultsQueue)
    assetsToCrawl <- library.getAssetsToCrawl()
    jobs = assetsToCrawl.map { case ChapterCrawl(site, assetTitle, url) =>
      SiteCrawlJob(
        site,
        ScrapeChaptersCrawlJob(url, assetTitle)
      )
    }
    _ <- crawler.enqueue(jobs)
    _ <- crawler.crawl()
    _ <- handleResults(resultsQueue, assetsToCrawl.length)
  } yield ()

  private def handleResults(
      queue: Queue[IO, Result],
      resultsToExpect: Int
  ): IO[Unit] =
    for {
      potentialResult <- queue.tryTake
      _ <- potentialResult match {
        case Some(result) => library.handleResult(result)
        case None         => IO.unit
      }
      _ <-
        if (resultsToExpect > 1) handleResults(queue, resultsToExpect - 1)
        else IO.unit
    } yield ()
