package library.services

import library.persistence
import cats._
import cats.implicits._
import library.domain.page._
import library.domain.asset.AssetId
import java.util.UUID

class PagesSuite extends munit.FunSuite:
  import PagesSuite._
  import common._

  test("Cant create the same page twice") {
    val page = CreateChaptersPage(
      Site("foo"),
      PageUrl("http://foo.bar/assets/baz"),
      AssetId(uuids.tail.head)
    )

    val pageStore = new persistence.Pages[Id]:
      override def create(page: CreateChaptersPage): Id[PageId] = ???
      override def findByUrl(url: PageUrl): Id[Option[ChaptersPage]] =
        ChaptersPage(PageId(uuids.head), page.assetId, page.site, page.url).some
      override def findManyByAssetIds(
          assetIds: List[AssetId]
      ): Id[List[ChaptersPage]] = ???
    val storage = persistence.Storage(
      uselessAssetsRepository,
      uselessChaptersRepository,
      pageStore
    )

    val result = Pages
      .make(storage)
      .create(page)
    assert(
      result.isLeft,
      "Creating the same page twice should result in failure"
    )
  }

object PagesSuite:
  val uuids = List(
    "0aed43c4-9be6-4d86-b418-dd0844d5a28a",
    "0aed43c4-9be6-4d86-b418-dd0844d5a28b",
    "0aed43c4-9be6-4d86-b418-dd0844d5a28c"
  ).map(UUID.fromString)
