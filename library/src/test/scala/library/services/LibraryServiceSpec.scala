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

  // Asset tests
  test("Cant add the same asset twice") {
    val assetDto = AssetDTO(
      name = "Foo",
      enabled = true,
      aliases = List(),
      titlePages = List()
    )

    val execute = createAsset(assetDto) *> createAsset(assetDto)

    execute.run(storage).map { secondAttempt =>
      assert(
        secondAttempt.isLeft,
        "Second attempt at adding the same asset didnt end in failure"
      )
    }
  }

  // Pages tests
  test("Cant add the same page twice") {
    val assetDto = AssetDTO(
      name = "Foo",
      enabled = true,
      aliases = List(),
      titlePages = List()
    )
    val pageDto = AssetPageDTO(site = "bar.baz", url = "http://bar.baz/foo")

    val execute = for {
      assetE <- createAsset(assetDto)
      pageInsertionResult <- assetE match {
        case Right(asset) =>
          createAssetPage(pageDto, asset.id) *> createAssetPage(
            pageDto,
            asset.id
          )
        case Left(reason) => Kleisli(_ => IO.pure(reason.asLeft[AssetPage]))
      }
    } yield pageInsertionResult

    execute(storage).map { result =>
      assert(
        result.isLeft,
        "Second attempt at adding the same page didnt end in failure"
      )
    }
  }

  // Chapters tests
  // TODO:
  // test("Cant add the same chapter twice") {}

  // Misc. tests
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
