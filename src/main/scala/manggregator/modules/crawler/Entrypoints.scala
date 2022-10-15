package manggregator.modules.crawler

import cats.data._
import cats.effect._
import manggregator.modules.crawler.domain.Library
import manggregator.modules.crawler.domain.Library.AssetToCrawl
import manggregator.modules.crawler.domain.Crawl.CrawlResult._
import manggregator.modules.crawler.domain.Asset.AssetSource
import manggregator.modules.crawler.services.CrawlingService
import manggregator.modules.library.domain.AssetRepository
import manggregator.modules.library.domain.Models.Chapter
import manggregator.modules.library.services.AssetService
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
          assetRepository.save(data).flatMap(_ => IO.println(result))

      }

    }

    CrawlingService.crawl().run(library)
  }
