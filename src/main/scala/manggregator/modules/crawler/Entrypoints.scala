package manggregator.modules.crawler

import manggregator.modules.library.domain.AssetRepository
import cats.data._
import cats.effect._
import manggregator.modules.crawler.domain.Library
import manggregator.modules.shared.domain.ChapterCrawl
import manggregator.modules.crawler.domain.Crawl.CrawlResult.Result
import manggregator.modules.library.services.AssetService
import manggregator.modules.crawler.services.CrawlingService

object Entrypoints:
  val crawler: Reader[AssetRepository, IO[Unit]] = Reader { assetRepository =>
    val library = new Library {
      def getAssetsToCrawl(): IO[List[ChapterCrawl]] =
        AssetService.getAssetsToCrawl().run(assetRepository)

      def handleResult(result: Result): IO[Unit] = IO.println(result)
    }

    CrawlingService.crawl().run(library)
  }
