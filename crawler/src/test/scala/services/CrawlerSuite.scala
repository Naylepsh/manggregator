package crawler.services

import munit.CatsEffectSuite
import crawler.domain.Asset._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Crawl.SiteCrawlJob
import crawler.domain.Crawl.CrawlResult
import crawler.domain.SiteCrawler
import cats._
import cats.effect._
import cats.effect.std._
import cats.implicits._
import java.util.Date
import java.util.UUID.randomUUID
import org.legogroup.woof.{given, *}

class CrawlerSuite extends CatsEffectSuite:
  import CrawlerSuite.{given, *}

  test("crawler processes all the jobs from queue") {
    val mapping: Map[String, SiteCrawler[IO]] =
      Map(testCrawlerLabel -> testCrawler)
    val jobs = List(
      SiteCrawlJob(
        testCrawlerLabel,
        ScrapeChaptersCrawlJob(
          "http://localhost:3000/assets/title-1",
          randomUUID
        )
      ),
      SiteCrawlJob(
        testCrawlerLabel,
        DiscoverTitlesCrawlJob("http://localhost:3000/assets", "Title 2")
      )
    )

    for {
      given Logger[IO] <- DefaultLogger.makeIo(noOutput)
      resultsQueue <- Queue.bounded[IO, CrawlResult.Result](capacity = 10)
      crawler <- Crawler.make[IO](resultsQueue, mapping)
      _ <- crawler.enqueue(jobs)
      _ <- crawler.crawl()
      resultsOnResultsQueue <- resultsQueue.size
    } yield assertEquals(resultsOnResultsQueue, jobs.length)
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
        randomUUID,
        "1",
        "http://localhost:3000/assets/title-1/chapters/1",
        Date()
      ),
      Chapter(
        randomUUID,
        "2",
        "http://localhost:3000/assets/title-1/chapters/2",
        Date()
      ),
      Chapter(
        randomUUID,
        "1",
        "http://localhost:3000/assets/title-2/chapters/1",
        Date()
      )
    )
  val testCrawlerLabel = "test"
  val testCrawler = new SiteCrawler[IO]:
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

  given Filter = Filter.everything
  given Printer = NoColorPrinter()

  def noOutput[F[_]: Applicative]: Output[F] = new Output[F]:
    def output(str: String) = ().pure
    def outputError(str: String) = output(str)
