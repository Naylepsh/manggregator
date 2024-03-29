package services

import java.util.Date
import java.util.UUID.randomUUID

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.implicits.*
import core.Url
import crawler.domain.Asset.*
import crawler.domain.Crawl.CrawlJob.*
import crawler.domain.Crawl.{CrawlResult, SiteCrawlJob}
import crawler.domain.SiteCrawler
import crawler.services.CrawlHandler
import munit.CatsEffectSuite
import org.legogroup.woof.{ *, given }

class CrawlHandlerSuite extends CatsEffectSuite:
  import CrawlHandlerSuite.{ *, given }

  test("crawler processes all the jobs from queue") {
    val mapping: Map[String, SiteCrawler[IO]] =
      Map(
        testCrawlerLabel        -> testCrawler,
        testFailingCrawlerLabel -> testFailingCrawler
      )
    val jobs = List(
      SiteCrawlJob(
        testCrawlerLabel,
        ScrapeChaptersCrawlJob(
          Url("http://localhost:3000/assets/title-1"),
          randomUUID
        )
      ),
      SiteCrawlJob(
        testCrawlerLabel,
        DiscoverTitlesCrawlJob(Url("http://localhost:3000/assets"), "Title 2")
      ),
      SiteCrawlJob(
        testFailingCrawlerLabel,
        DiscoverTitlesCrawlJob(Url("http://localhost:3000/assets"), "Title 3")
      )
    )

    for
      given Logger[IO] <- DefaultLogger.makeIo(noOutput)
      resultsQueue     <- Queue.bounded[IO, CrawlResult.Result](capacity = 10)
      crawlQueue       <- Queue.bounded[IO, SiteCrawlJob](capacity = 10)
      crawler = CrawlHandler.make[IO](crawlQueue, resultsQueue, mapping)
      _                     <- jobs.traverse(crawlQueue.offer)
      _                     <- crawler.crawl()
      resultsOnResultsQueue <- resultsQueue.size
    yield assertEquals(resultsOnResultsQueue, jobs.length)
  }

object CrawlHandlerSuite:
  val testTitles =
    List(
      AssetSource("Title 1", Url("http://localhost:3000/assets/title-1")),
      AssetSource("Title 2", Url("http://localhost:3000/assets/title-2"))
    )
  val testChapters =
    List(
      Chapter(
        randomUUID,
        "1",
        Url("http://localhost:3000/assets/title-1/chapters/1"),
        Date()
      ),
      Chapter(
        randomUUID,
        "2",
        Url("http://localhost:3000/assets/title-1/chapters/2"),
        Date()
      ),
      Chapter(
        randomUUID,
        "1",
        Url("http://localhost:3000/assets/title-2/chapters/1"),
        Date()
      )
    )
  val testCrawlerLabel = "test"
  val testCrawler = new SiteCrawler[IO]:
    def discoverTitles(
        job: DiscoverTitlesCrawlJob
    ): IO[Either[Throwable, List[AssetSource]]] =
      testTitles.asRight.pure

    def scrapeChapters(
        job: ScrapeChaptersCrawlJob
    ): IO[Either[Throwable, List[Chapter]]] =
      testChapters.asRight.pure

  val testFailingCrawlerLabel = "test-failing"
  val testFailingCrawler = new SiteCrawler[IO]:
    def discoverTitles(
        job: DiscoverTitlesCrawlJob
    ): IO[Either[Throwable, List[AssetSource]]] =
      new RuntimeException("Failed for reasons").asLeft.pure

    def scrapeChapters(
        job: ScrapeChaptersCrawlJob
    ): IO[Either[Throwable, List[Chapter]]] =
      new RuntimeException("Failed for reasons").asLeft.pure

  given Filter  = Filter.everything
  given Printer = NoColorPrinter()

  def noOutput[F[_]: Applicative]: Output[F] = new Output[F]:
    def output(str: String)      = ().pure
    def outputError(str: String) = output(str)
