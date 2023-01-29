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
import java.util.Date
import library.suite.DatabaseSuite

object ChaptersSuite extends DatabaseSuite:

  testWithCleanDb("Created chapters can be found") { xa =>
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

  testWithCleanDb(
    "Searching for recent released does not include records released before given date"
  ) { xa =>
    val assetRepository = Assets.makeSQL(xa)
    val chapterRepository = Chapters.makeSQL(xa)

    val asset = CreateAsset(AssetName("asset1"), Enabled(true))
    val oldRelease = new SimpleDateFormat("dd/MM/yyyy").parse("04/01/2023")
    val newRelease = new SimpleDateFormat("dd/MM/yyyy").parse("05/01/2023")
    val newestRelease = new SimpleDateFormat("dd/MM/yyyy").parse("06/01/2023")

    for
      assetId <- assetRepository.create(asset)
      _ <- createChapter(chapterRepository, "1", oldRelease, assetId)
      newId <- createChapter(chapterRepository, "2", newRelease, assetId)
      newestId <- createChapter(chapterRepository, "3", newestRelease, assetId)
      recentChapters <- chapterRepository.findRecentReleases(
        DateReleased(newRelease)
      )
    yield expect.all(
      recentChapters.length == 2,
      List(newId, newestId).forall(recentChapters.map(_.id).contains)
    )
  }

  private def createChapter(
      chapterRepository: Chapters[IO],
      no: String,
      date: Date,
      assetId: AssetId
  ) =
    chapterRepository
      .create(
        List(
          CreateChapter(
            no = ChapterNo(no),
            url = ChapterUrl(s"http://foo.bar/asset/$no"),
            dateReleased = DateReleased(date),
            assetId = assetId
          )
        )
      )
      .map(_.head)
