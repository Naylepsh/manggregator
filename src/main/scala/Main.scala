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
import manggregator.modules.crawler.Entrypoints._
import manggregator.modules.crawler.Entrypoints
import manggregator.modules.crawler.domain.Library.AssetToCrawl
import manggregator.modules.library.services.AssetRepositoryImpl.AssetInMemoryRepository
import manggregator.modules.library.domain.Models._
import java.util.UUID.randomUUID

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = crawlEntrypoint

  def crawlEntrypoint: IO[ExitCode] =
    val repo = AssetInMemoryRepository

    val eliteKnight = Asset(randomUUID, "Elite Knight", true, List())
    val eliteKnightPage =
      AssetPage(
        randomUUID,
        eliteKnight.id,
        "mangakakalot",
        "https://readmanganato.com/manga-gx984006"
      )
    val saisa = Asset(randomUUID, "Saisa", true, List())
    val saisaPage =
      AssetPage(
        randomUUID,
        saisa.id,
        "mangakakalot",
        "https://mangakakalot.com/manga/2_saisa_no_osananajimi"
      )

    for {
      _ <- repo.save(eliteKnight)
      _ <- repo.save(eliteKnightPage)
      _ <- repo.save(saisa)
      _ <- repo.save(saisaPage)
      _ <- Entrypoints.crawler.run(repo)
    } yield ExitCode.Success

  def testCrawler: IO[ExitCode] =
    val library = new Library {
      def getAssetsToCrawl(): IO[List[AssetToCrawl]] = IO(
        List(
          AssetToCrawl(
            "mangakakalot",
            "Elite Knight",
            "https://readmanganato.com/manga-gx984006"
          ),
          AssetToCrawl(
            "mangakakalot",
            "Saisa",
            "https://mangakakalot.com/manga/2_saisa_no_osananajimi"
          )
        )
      )

      def handleResult(result: Result): IO[Unit] = IO.println(result)
    }

    CrawlingService.crawl().run(library).as(ExitCode.Success)

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
