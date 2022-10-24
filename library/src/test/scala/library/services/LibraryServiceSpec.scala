package library.services

import LibraryService._
import library.domain.Models._
import java.util.UUID.randomUUID
import cats.effect.IO
import cats._
import cats.implicits._
import cats.syntax._
import cats.data.Kleisli
import services.PageRepositoryImpl

class LibraryServiceSpec extends munit.FunSuite:
  import LibraryServiceSpec._

  test("Pages added to a given asset appear when getting the assets to crawl") {
    val firstPage = AssetPageDTO(site = "bar.baz", url = "http://bar.baz/foo")
    val secondPage =
      AssetPageDTO(site = "qux.quux", url = "http://qux.quux/assets/foo")
    val assetDto = AssetDTO(
      name = "Foo",
      enabled = true,
      aliases = List(),
      titlePages = List(firstPage)
    )

    val seedAndGetResults: Kleisli[IO, Storage, List[AssetToCrawl]] =
      for {
        assetE <- createAsset(assetDto)
        _ <- assetE match {
          case Right(asset) => createAssetPage(secondPage, asset.id)
          case Left(reason) => Kleisli(_ => IO.pure(reason.asLeft[AssetPage]))
        }
        assetsToCrawl <- getAssetsToCrawl()
      } yield assetsToCrawl

    seedAndGetResults
      .run(storage)
      .map(assetsToCrawl =>
        assertEquals(assetsToCrawl.length, 2)

        assertEquals(
          assetsToCrawl
            .filter(a => List(firstPage.site, secondPage.site).contains(a.site))
            .length,
          assetsToCrawl.length
        )
      )
  }

object LibraryServiceSpec:
  val storage = Storage(
    AssetRepositoryImpl.inMemoryRepository,
    PageRepositoryImpl.inMemoryRepository,
    ChapterRepositoryImpl.inMemoryRepository
  )
