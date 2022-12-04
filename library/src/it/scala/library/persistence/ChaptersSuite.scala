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
import library.domain.chapter._

object ChaptersSuite extends IOSuite:
  override type Res = HikariTransactor[IO]
  override def sharedResource: Resource[cats.effect.IO, Res] =
    databaseResource
      .evalTap { xa =>
        clearChapters(xa) *> clearAssets(xa)
      }

  test("Created chapters can be found") { xa =>
    val assetRepository = Assets.makeSQL(xa)
    val chapterRepository = Chapters.makeSQL(xa)

    val asset1 = CreateAsset(AssetName("asset1"), Enabled(true))
    val asset2 = CreateAsset(AssetName("asset2"), Enabled(true))
    val asset3 = CreateAsset(AssetName("asset3"), Enabled(true))
    val assets = List(asset1, asset2, asset3)

    val date = new SimpleDateFormat("dd/MM/yyyy").parse("03/12/2022")

    for
      assetIds <- assets.traverse(assetRepository.create)
      chapters = assetIds.zipWithIndex.map { case (assetId, index) =>
        CreateChapter(
          no = ChapterNo(index.toString),
          url = ChapterUrl(s"http://foo.bar/asset/$index"),
          dateReleased = DateReleased(date),
          assetId = assetId
        )
      }
      assetIdsSubset = assetIds.tail
      chaptersBefore <- chapterRepository.findByAssetIds(assetIdsSubset)
      chaptersIds <- chapterRepository.create(chapters)
      chaptersAfter <- chapterRepository.findByAssetIds(assetIdsSubset)
    yield expect.all(
      assetIds.length == assets.length,
      chaptersBefore.length == 0,
      chaptersIds.length == assetIds.length,
      chaptersAfter.length == assetIdsSubset.length
    )
  }

  private def clearChapters(xa: HikariTransactor[IO]) =
    sql"""
    DELETE FROM chapter
    """.update.run.void.transact(xa)
