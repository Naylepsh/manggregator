package library.persistence

import weaver._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import java.text.SimpleDateFormat
import library.resources.database._
import library.config.types._
import library.domain.asset._
import library.domain.page._

object PagesSuite extends IOSuite:
  override type Res = HikariTransactor[IO]
  override def sharedResource: Resource[cats.effect.IO, Res] =
    databaseResource
      .evalTap { xa =>
        clearPages(xa) *> clearAssets(xa)
      }

  test("Created pages can be found") { xa =>
    val assetRepository = Assets.makeSQL(xa)
    val pagesRepository = Pages.makeSQL(xa)

    val asset1 = CreateAsset(AssetName("asset1"), Enabled(true))
    val asset2 = CreateAsset(AssetName("asset2"), Enabled(true))
    val asset3 = CreateAsset(AssetName("asset3"), Enabled(true))
    val assets = List(asset1, asset2, asset3)

    val date = new SimpleDateFormat("dd/MM/yyyy").parse("03/12/2022")

    for
      assetIds <- assets.traverse(assetRepository.create)
      pages = assetIds.map { assetId =>
        CreateChaptersPage(
          site = Site("some-site"),
          url = PageUrl(s"http://some-site.com/asset/${assetId.value}"),
          assetId = assetId
        )
      }
      assetIdsSubset = assetIds.tail
      pagesBefore <- pagesRepository.findByAssetIds(assetIdsSubset)
      pagesIds <- pages.traverse(pagesRepository.create)
      pagesAfter <- pagesRepository.findByAssetIds(assetIdsSubset)
    yield expect.all(
      assetIds.length == assets.length,
      pagesBefore.length == 0,
      pagesIds.length == assetIds.length,
      pagesAfter.length == assetIdsSubset.length
    )
  }

  private def clearPages(xa: HikariTransactor[IO]) =
    sql"""
    DELETE FROM chapters_page
    """.update.run.void.transact(xa)
