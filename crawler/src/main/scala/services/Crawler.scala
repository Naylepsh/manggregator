package crawler.services

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Queue
import org.legogroup.woof.{given, *}
import scala.concurrent.duration._
import scala.language.postfixOps
import crawler.domain._
import crawler.domain.Crawl._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.SiteCrawler
import crawler.services.site_crawlers.MangakakalotCrawler

trait Crawler[F[_]]:
  def crawl(): F[Unit]
  def enqueue(jobs: List[SiteCrawlJob]): F[Unit]

object Crawler:
  def make[F[_]: Async: Logger](
      resultsQueue: Queue[F, Result],
      siteCrawlersMapping: Map[String, SiteCrawler[F]]
  ): F[Crawler[F]] =
    for {
      crawlQueue <- Queue.bounded[F, SiteCrawlJob](capacity = 10)
    } yield makeMonadicCrawler(siteCrawlersMapping, crawlQueue, resultsQueue)

  def makeMonadicCrawler[F[_]: Monad: Logger](
      siteCrawlersMappings: Map[String, SiteCrawler[F]],
      crawlQueue: Queue[F, SiteCrawlJob],
      resultQueue: Queue[F, Result]
  ): Crawler[F] = new Crawler[F] {
    // TODO: Allow more executors + round robin or whatever
    private val execute = CrawlExecutor.crawl(siteCrawlersMappings)

    override def crawl(): F[Unit] =
      for {
        potentialJob <- crawlQueue.tryTake
        _ <- potentialJob
          .map(job =>
            (execute(job) match {
              case Left(reason) =>
                Logger[F].error(reason)

              case Right(results) =>
                results.flatMap(_.traverse(resultQueue.offer)).void
            }).flatMap(_ => crawl())
          )
          .getOrElse(Monad[F].unit)
      } yield ()

    override def enqueue(jobs: List[SiteCrawlJob]): F[Unit] =
      jobs.traverse(crawlQueue.offer).void

  }
