import manggregator.modules.crawler.services.site_crawlers.MangakakalotCrawler
import manggregator.modules.crawler.domain.Crawl.CrawlJob
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect._
import cats._
import cats.implicits._
import manggregator.modules.crawler.domain.Asset.Chapter

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
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
