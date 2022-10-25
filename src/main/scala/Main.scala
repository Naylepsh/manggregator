import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect._
import cats._
import cats.implicits._
import crawler.domain.Asset.Chapter
import crawler.domain.Crawl.CrawlJob
import crawler.domain.Crawl.CrawlResult.Result
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl
import crawler.services.CrawlingService
import crawler.services.site_crawlers.MangakakalotCrawler
import library.domain.Models._
import library.services.LibraryService.Storage
import java.util.UUID.randomUUID
import manggregator.Entrypoints
import api.Http
import library.domain.AssetRepository
import library.services.AssetRepositoryImpl
import library.services.ChapterRepositoryImpl
import services.PageRepositoryImpl

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val assetRepo = AssetRepositoryImpl.inMemoryRepository
    val chapterRepo = ChapterRepositoryImpl.inMemoryRepository
    val pagesRepo = PageRepositoryImpl.inMemoryRepository
    val storage = Storage(assetRepo, pagesRepo, chapterRepo)
    val library = Entrypoints.library(storage)

    seedAssetRepository(assetRepo) *> httpServer(storage, library)

  def httpServer(storage: Storage, library: Library): IO[ExitCode] =

    val docs = Http.Docs(title = "MANGgregator", version = "0.0.1")
    val server = api.Http(Http.Props(docs, library, storage))

    server.as(ExitCode.Success)

  def seedAssetRepository(repo: AssetRepository) =
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

    println(s"${eliteKnight.id} :: ${saisa.id}")

    for {
      _ <- repo.save(eliteKnight)
      _ <- repo.save(saisa)
    } yield repo
