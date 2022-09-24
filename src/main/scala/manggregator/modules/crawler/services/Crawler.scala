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
import manggregator.modules.crawler.domain.site_crawlers.SiteCrawler

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
            case Left(reason)   => IO.println(reason)
            case Right(results) => results.flatMap(resultQueue.offer)
          } flatMap (_ => crawl())
      }
    } yield ()

  def enqueue(jobs: List[SiteCrawlJob]): IO[Unit] =
    jobs.foldLeft(IO.unit)((_, job) => crawlQueue.offer(job))
