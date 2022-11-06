package manggregator

import cats.data._
import cats.effect._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Asset.AssetSource
import crawler.services._
import crawler.services.site_crawlers.MangakakalotCrawler
import java.util.UUID.randomUUID
import library.persistence.Storage
import library.persistence
import library.services._
import library.domain.page.ChaptersPageToCheck
import library.domain.chapter._
import library.domain.asset.AssetId

object Entrypoints:
  def storage(): Storage[IO] = Storage(
    persistence.Assets.make[IO],
    persistence.Chapters.make[IO],
    persistence.Pages.make[IO]
  )

  def library(storage: Storage[IO]): Library[IO] = new Library {
    def getAssetsToCrawl(): IO[List[AssetToCrawl]] =
      Pages
        .findPagesToCheck()
        .run(storage)
        .map(_.map { case ChaptersPageToCheck(site, url, title) =>
          AssetToCrawl(site.value, title.value, url.value)
        })

    def handleResult(result: Result): IO[Unit] = result match {
      case ChapterResult(chapters) =>
        Chapters
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
          .run(storage.chapters)
          .void
    }
  }

  def crawling(): Crawling[IO] =
    val siteCrawlersMapping = Map(
      "mangakakalot" -> MangakakalotCrawler
    )

    Crawling.make[IO](siteCrawlersMapping)
