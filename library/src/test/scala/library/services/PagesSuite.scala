package library.services

import java.util.UUID

import cats.*
import cats.effect.IO
import cats.implicits.*
import library.domain.asset.*
import library.domain.page.*
import library.persistence

import common.*

class PagesSuite extends munit.FunSuite:
  import PagesSuite.*

  test("Cant create the same page twice") {
    val page = CreateChaptersPage(
      samplePage.site,
      samplePage.url,
      samplePage.assetId
    )

    val storage = persistence.Storage(
      new TestAssets[IO],
      new TestChapters[IO],
      dataPages(samplePage)
    )

    Pages
      .make(storage)
      .create(page)
      .map(result =>
        assert(
          result.isLeft,
          "Creating the same page twice should result in failure"
        )
      )
  }

object PagesSuite:
  val samplePage = ChaptersPage(
    PageId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a")),
    AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28b")),
    Site("foo"),
    PageUrl("http://foo.bar/assets/baz")
  )

  def dataPages(page: ChaptersPage) = new TestPages[IO]:
    override def findByUrl(url: PageUrl): IO[Option[ChaptersPage]] =
      page.some.pure
