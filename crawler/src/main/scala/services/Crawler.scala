package crawler.services

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Queue
import scala.concurrent.duration._
import scala.language.postfixOps
import crawler.domain._
import crawler.domain.Crawl._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.SiteCrawler
import crawler.services.site_crawlers.MangakakalotCrawler

class Crawler(
    siteCrawlersMappings: Map[String, SiteCrawler],
    crawlQueue: Queue[IO, SiteCrawlJob],
    resultQueue: Queue[IO, Result]
):
  // TODO: Allow more executors + round robin or whatever
  val execute = CrawlExecutor.crawl(siteCrawlersMappings)

  def keepCrawling() = (crawl() *> IO.sleep(5 seconds)).foreverM

  // Picks up crawl jobs until the queue is empty
  def crawl(): IO[Unit] =
    for {
      potentialJob <- crawlQueue.tryTake
      _ <- potentialJob
        .map(job =>
          (execute(job) match {
            case Left(reason) =>
              IO.println(reason)

            case Right(results) =>
              results.flatMap(_.traverse(resultQueue.offer))
          }) *> crawl()
        )
        .getOrElse(IO.unit)
    } yield ()

  def enqueue(jobs: List[SiteCrawlJob]): IO[Unit] =
    jobs.traverse(crawlQueue.offer).void

object Crawler:
  def apply(resultQueue: Queue[IO, Result]): IO[Crawler] =
    val siteCrawlersMappings = Map(
      "mangakakalot" -> MangakakalotCrawler
    )

    Queue
      .bounded[IO, SiteCrawlJob](capacity = 10)
      .map(crawlQueue =>
        new Crawler(siteCrawlersMappings, crawlQueue, resultQueue)
      )
