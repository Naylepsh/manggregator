package crawler.services

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import cats.effect.std._
import cats.effect.implicits._
import scala.concurrent.duration._
import scala.language.postfixOps
import crawler.domain.Crawl._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl

trait Crawling[F[_]]:
  def crawl(): Kleisli[F, Library[F], Unit]

object Crawling:
  def make[F[_]: Async](
      siteCrawlersMapping: SiteCrawlersMapping[F]
  ): Crawling[F] = new Crawling[F]:

    override def crawl(): Kleisli[F, Library[F], Unit] = Kleisli { library =>
      for {
        resultsQueue <- Queue.bounded[F, Result](capacity = 10)
        crawler <- Crawler.make[F](resultsQueue, siteCrawlersMapping)
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
        queue: Queue[F, Result],
        library: Library[F],
        resultsToExpect: Int
    ): F[Unit] =
      def handle(resultsLeft: Int): F[Unit] =
        // TODO: Actual log
        // println(s"[Crawling Service] resultsLeft: $resultsLeft")

        if (resultsLeft > 0)
          for {
            potentialResult <- queue.tryTake
            _ <- potentialResult match {
              case Some(result) =>
                library.handleResult(result) *> handle(resultsLeft - 1)

              case None =>
                Async[F].sleep(5 seconds) *> handle(resultsLeft)
            }
          } yield ()
        else Async[F].unit

      handle(resultsToExpect)
