package crawler.services

import cats._
import cats.data._
import cats.effect._
import cats.effect.implicits._
import cats.effect.std._
import cats.implicits._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Crawl._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl
import org.legogroup.woof.{_, given}
import services.ResultHandler

trait Crawler[F[_]]:
  def crawl(): Kleisli[F, Library[F], Unit]

object Crawler:
  def make[F[_]: Async: Logger](
      siteCrawlersMapping: SiteCrawlersMapping[F]
  ): Crawler[F] = new Crawler[F]:

    override def crawl(): Kleisli[F, Library[F], Unit] = Kleisli { library =>
      for {
        assetsToCrawl <- library.getAssetsToCrawl()
        resultsQueue <- Queue.unbounded[F, Result]
        crawlQueue <- Queue.unbounded[F, SiteCrawlJob]
        handler = ResultHandler.make[F](resultsQueue, library)
        crawler = CrawlHandler
          .makeCluster[F](
            crawlQueue,
            resultsQueue,
            siteCrawlersMapping,
            assetsToCrawl.length
          )
        jobs = assetsToCrawl.map { case AssetToCrawl(site, assetId, url) =>
          SiteCrawlJob(
            site,
            ScrapeChaptersCrawlJob(url, assetId)
          )
        }
        _ <- Logger[F].debug("Putting jobs on the crawl queue")
        _ <- jobs.traverse(crawlQueue.offer).void
        _ <- Logger[F].debug("Starting the crawl")
        _ <- (
          crawler.crawl(),
          handler.handle(assetsToCrawl.length)
        ).parTupled.void
        _ <- Logger[F].debug("Done with the crawl")
      } yield ()
    }
