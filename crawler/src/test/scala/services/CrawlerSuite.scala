package crawler.services

import munit.CatsEffectSuite
import crawler.domain.Asset._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Crawl.SiteCrawlJob
import crawler.domain.Crawl.CrawlResult
import crawler.domain.SiteCrawler
import cats.effect._
import cats.effect.std._
import cats.implicits._
import java.util.Date

class CrawlerSuite extends CatsEffectSuite:
  import CrawlerSuite._

  test("crawler processes all the jobs from queue") {
    val mapping: Map[String, SiteCrawler] = Map(testCrawlerLabel -> testCrawler)
    val jobs = List(
      SiteCrawlJob(
        testCrawlerLabel,
        ScrapeChaptersCrawlJob(
          "http://localhost:3000/assets/title-1",
          "Title 1"
        )
      ),
      SiteCrawlJob(
        testCrawlerLabel,
        DiscoverTitlesCrawlJob("http://localhost:3000/assets", "Title 2")
      )
    )

    for {
      resultsQueue <- Queue.bounded[IO, CrawlResult.Result](capacity = 10)
      crawlQueue <- Queue.bounded[IO, SiteCrawlJob](capacity = 10)
      _ <- jobs.traverse(crawlQueue.offer)
      crawler = new Crawler(mapping, crawlQueue, resultsQueue)
      _ <- crawler.crawl()
      resultsOnResultsQueue <- resultsQueue.size
    } yield assertEquals(resultsOnResultsQueue, jobs.length)
  }

  test("enqueing puts jobs on crawl queue") {
    val jobs = List(
      SiteCrawlJob(
        testCrawlerLabel,
        ScrapeChaptersCrawlJob(
          "http://localhost:3000/assets/title-1",
          "Title 1"
        )
      ),
      SiteCrawlJob(
        testCrawlerLabel,
        DiscoverTitlesCrawlJob("http://localhost:3000/assets", "Title 2")
      )
    )

    for {
      resultsQueue <- Queue.bounded[IO, CrawlResult.Result](capacity = 10)
      crawlQueue <- Queue.bounded[IO, SiteCrawlJob](capacity = 10)
      crawler = new Crawler(
        Map[String, SiteCrawler](),
        crawlQueue,
        resultsQueue
      )
      _ <- crawler.enqueue(jobs)
      resultsOnCrawlQueue <- crawlQueue.size
    } yield assertEquals(resultsOnCrawlQueue, jobs.length)
  }

object CrawlerSuite:
  val testTitles =
    List(
      AssetSource("Title 1", "http://localhost:3000/assets/title-1"),
      AssetSource("Title 2", "http://localhost:3000/assets/title-2")
    )
  val testChapters =
    List(
      Chapter(
        "Title 1",
        "1",
        "http://localhost:3000/assets/title-1/chapters/1",
        Date()
      ),
      Chapter(
        "Title 1",
        "2",
        "http://localhost:3000/assets/title-1/chapters/2",
        Date()
      ),
      Chapter(
        "Title 2",
        "1",
        "http://localhost:3000/assets/title-2/chapters/1",
        Date()
      )
    )
  val testCrawlerLabel = "test"
  val testCrawler = new SiteCrawler:
    def discoverTitles(
        job: DiscoverTitlesCrawlJob
    ): IO[Either[Throwable, List[AssetSource]]] = IO(
      Right(testTitles)
    )

    def scrapeChapters(
        job: ScrapeChaptersCrawlJob
    ): IO[Either[Throwable, List[Chapter]]] = IO(
      Right(testChapters)
    )
