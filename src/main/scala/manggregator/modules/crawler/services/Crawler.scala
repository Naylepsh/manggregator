package manggregator.modules.crawler.services

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Queue
import scala.concurrent.duration._
import scala.language.postfixOps
import manggregator.modules.crawler.domain._
import manggregator.modules.crawler.domain.Crawl._
import manggregator.modules.crawler.domain.Crawl.CrawlResult._
import manggregator.modules.crawler.domain.SiteCrawler
import manggregator.modules.crawler.services.site_crawlers.MangakakalotCrawler

class Crawler(
    siteCrawlersMappings: Map[String, SiteCrawler],
    crawlQueue: Queue[IO, SiteCrawlJob],
    resultQueue: Queue[IO, Result]
):
  // TODO: Allow more executors + round robin or whatever
  val executor = CrawlExecutor.crawl(siteCrawlersMappings)

  def keepCrawling() = crawl().map(_ => IO.sleep(5 seconds)).foreverM

  // Picks up crawl jobs until the queue is empty
  def crawl(): IO[Unit] =
    for {
      potentialJob <- crawlQueue.tryTake
      resultsE <- potentialJob match {
        case None => IO.unit
        case Some(job) =>
          executor(job) match {
            case Left(reason) => IO.println(reason)
            case Right(results) =>
              results.flatMap(_.map(resultQueue.offer).sequence)
          } flatMap (_ => crawl())
      }
    } yield ()

  def enqueue(jobs: List[SiteCrawlJob]): IO[Unit] =
    jobs.traverse(crawlQueue.offer).as(())

object Crawler:
  def apply(resultQueue: Queue[IO, Result]): IO[Crawler] = for {
    crawlQueue <- Queue.bounded[IO, SiteCrawlJob](capacity = 10)
    siteCrawlersMappings = Map(
      "mangakakalot" -> MangakakalotCrawler
    )
  } yield new Crawler(siteCrawlersMappings, crawlQueue, resultQueue)
