package manggregator

import java.util.UUID.randomUUID

import api.config._
import api.library.routes.Services
import api.{HttpApi, HttpServer}
import cats.data._
import cats.effect._
import crawler.domain.Asset.AssetSource
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl
import crawler.services._
import crawler.services.site_crawlers.MangakakalotCrawler
import doobie.util.transactor.Transactor
import library.domain.asset.AssetId
import library.domain.chapter._
import library.domain.page.ChaptersPageToCheck
import library.persistence
import library.persistence.Storage
import library.services._
import org.legogroup.woof.{_, given}

object Entrypoints:
  def logger(): IO[Logger[IO]] =
    given Filter = Filter.everything
    given Printer = ColorPrinter()

    DefaultLogger.makeIo(Output.fromConsole)

  def storage(xa: Transactor[IO]): Storage[IO] = Storage(
    persistence.Assets.makeSQL[IO](xa),
    persistence.Chapters.makeSQL[IO](xa),
    persistence.Pages.makeSQL[IO](xa)
  )

  def library(storage: Storage[IO]): Library[IO] = new Library {
    def getAssetsToCrawl(): IO[List[AssetToCrawl]] =
      Pages
        .make(storage)
        .findPagesOfEnabledAssets()
        .map(_.map { case ChaptersPageToCheck(site, url, title) =>
          AssetToCrawl(site.value, title.value, url.value)
        })

    def handleResult(result: SuccessfulResult): IO[Unit] = result match {
      case ChapterResult(chapters) =>
        Chapters
          .make(storage.chapters)
          .create(
            chapters.map(chapter =>
              CreateChapter(
                ChapterNo(chapter.no),
                ChapterUrl(chapter.url),
                DateReleased(chapter.dateReleased),
                AssetId(chapter.assetId)
              )
            )
          )
          .void
    }
  }

  def crawling()(using Logger[IO]): Crawler[IO] =
    val siteCrawlersMapping = Map(
      "mangakakalot" -> MangakakalotCrawler
    )


    Crawler.make[IO](siteCrawlersMapping)

  def libraryServices(storage: Storage[IO]): Services[IO] = Services(
    Assets.make(storage),
    Pages.make(storage)
  )

  def http(
      docs: Docs,
      library: Library[IO],
      crawling: Crawler[IO],
      libraryServices: Services[IO],
      serverConfig: ServerConfig
  )(using Logger[IO]) =
    val api = HttpApi.make[IO](docs, library, crawling, libraryServices)
    HttpServer[IO].newEmber(serverConfig, api.app)
