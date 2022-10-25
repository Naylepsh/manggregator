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
import library.services.LibraryService
import java.util.UUID.randomUUID
import library.services.LibraryService.Storage
import library.services.LibraryService.ChapterDTO.apply
import library.services.LibraryService.ChapterDTO

object Entrypoints:
  val crawler: Reader[Storage, IO[Unit]] = Reader { storage =>
    val library = new Library {
      def getAssetsToCrawl(): IO[List[AssetToCrawl]] =
        LibraryService
          .getAssetsToCrawl()
          .run(storage)
          .map(_.map { case LibraryService.AssetToCrawl(site, url, title) =>
            AssetToCrawl(site, title, url)
          })

      def handleResult(result: Result): IO[Unit] = result match {
        case ChapterResult(chapters) =>
          LibraryService
            .saveChapters(
              chapters.map(chapter =>
                ChapterDTO(
                  chapter.no,
                  chapter.url,
                  chapter.dateReleased,
                  chapter.assetId
                )
              )
            )
            .run(storage)
            .void
      }

    }

    CrawlingService.crawl().run(library)
  }

  def library(storage: Storage): Library = new Library {
    def getAssetsToCrawl(): IO[List[AssetToCrawl]] =
      LibraryService
        .getAssetsToCrawl()
        .run(storage)
        .map(_.map { case LibraryService.AssetToCrawl(site, url, title) =>
          AssetToCrawl(site, title, url)
        })

    def handleResult(result: Result): IO[Unit] = result match {
      case ChapterResult(chapters) =>
        val data = chapters.map(chapter =>
          Chapter(
            randomUUID,
            chapter.no,
            chapter.url,
            chapter.dateReleased,
            chapter.assetId
          )
        )
        storage.chapters.save(data)
    }
  }
