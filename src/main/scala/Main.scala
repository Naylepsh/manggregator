import manggregator.modules.crawler.services.site_crawlers.MangakakalotCrawler
import manggregator.modules.crawler.domain.Crawl.CrawlJob
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect._
import cats._
import cats.implicits._
import manggregator.modules.crawler.domain.Asset.Chapter
import manggregator.modules.crawler.services.CrawlingService
import manggregator.modules.crawler.domain.Crawl.CrawlResult.Result
import manggregator.modules.crawler.domain.Library
import manggregator.modules.crawler.domain.ChapterCrawl

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = testCrawler

  def testCrawler: IO[ExitCode] =
    val library = new Library {
      def getAssetsToCrawl(): IO[List[ChapterCrawl]] = IO(
        List(
          ChapterCrawl(
            "mangakakalot",
            "Elite Knight",
            "https://readmanganato.com/manga-gx984006"
          ),
          ChapterCrawl(
            "mangakakalot",
            "Saisa",
            "https://mangakakalot.com/manga/2_saisa_no_osananajimi"
          )
        )
      )

      def handleResult(result: Result): IO[Unit] = IO.println(result)
    }

    val service = CrawlingService(library)

    service.crawl().as(ExitCode.Success)

  def testScraper: IO[ExitCode] =
    // val results = MangakakalotCrawler.scrapeChapters(
    //   CrawlJob.ScrapeChaptersCrawlJob(
    //     "https://mangakakalot.com/manga/ot927321",
    //     "Karami Zakari"
    //   )
    // )

    val results = MangakakalotCrawler.scrapeChapters(
      CrawlJob.ScrapeChaptersCrawlJob(
        "https://readmanganato.com/manga-gx984006",
        "Older Elite Knight"
      )
    )

    results.flatMap(showResults).as(ExitCode.Success)

  def showResults(results: Either[Throwable, List[Chapter]]): IO[Unit] =
    results match {
      case Left(reason)    => IO.println(reason)
      case Right(chapters) => chapters.traverse(IO.println).as(())
    }
