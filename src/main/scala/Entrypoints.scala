package manggregator

import cats.data._
import cats.effect._
import crawler.domain.Library
import crawler.domain.Library.AssetToCrawl
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Asset.AssetSource
import crawler.services.CrawlingService
import library.domain.AssetRepository
import library.domain.Models.Chapter
import library.services.AssetService
import java.util.UUID.randomUUID

object Entrypoints:
  val crawler: Reader[AssetRepository, IO[Unit]] = Reader { assetRepository =>
    val library = new Library {
      def getAssetsToCrawl(): IO[List[AssetToCrawl]] =
        AssetService
          .getAssetsToCrawl()
          .run(assetRepository)
          .map(_.map { case AssetService.AssetToCrawl(site, url, title) =>
            AssetToCrawl(site, title, url)
          })

      def handleResult(result: Result): IO[Unit] = result match {
        case ChapterResult(chapters) =>
          val data = chapters.map(chapter =>
            Chapter(
              randomUUID,
              chapter.no,
              chapter.assetTitle,
              chapter.url,
              chapter.dateReleased
            )
          )
          assetRepository.save(data)

      }

    }

    CrawlingService.crawl().run(library)
  }
