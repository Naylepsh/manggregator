package crawler.services

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import cats.effect.std._
import cats.effect.implicits._
import org.legogroup.woof.{given, *}
import crawler.domain.Crawl._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl
import services.ResultHandler

trait Crawling[F[_]]:
  def crawl(): Kleisli[F, Library[F], Unit]

object Crawling:
  def make[F[_]: Async: Logger](
      siteCrawlersMapping: SiteCrawlersMapping[F]
  ): Crawling[F] = new Crawling[F]:

    override def crawl(): Kleisli[F, Library[F], Unit] = Kleisli { library =>
      for {
        assetsToCrawl <- library.getAssetsToCrawl()
        resultsQueue <- Queue.bounded[F, Result](capacity = 10)
        crawlQueue <- Queue.bounded[F, SiteCrawlJob](capacity = 10)
        handler = ResultHandler.make[F](resultsQueue, library)
        crawler = Crawler
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
        _ <- jobs.traverse(crawlQueue.offer).void
        _ <- (
          crawler.crawl(),
          handler.handle(assetsToCrawl.length)
        ).parTupled.void
      } yield ()
    }
