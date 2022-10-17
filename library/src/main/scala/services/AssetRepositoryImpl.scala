package library.services

import library.domain.AssetRepository
import library.domain.Models._
import cats.effect.IO
import scala.collection.mutable.Map as MutableMap
import scala.collection.mutable.ListBuffer
import java.util.UUID

object AssetRepositoryImpl:
  object AssetInMemoryRepository extends AssetRepository:
    val assetsStore: MutableMap[UUID, Asset] = MutableMap()
    val assetPagesStore: ListBuffer[AssetPage] = ListBuffer()
    val chapterStore: ListBuffer[Chapter] = ListBuffer()

    def findByName(name: String): IO[Option[Asset]] = IO(
      assetsStore
        .find { case (_, asset) => asset.name == name }
        .map { case (_, asset) => asset }
    )

    def save(asset: Asset): IO[Unit] = IO(assetsStore.addOne(asset.id -> asset))

    def save(assetPage: AssetPage): IO[Unit] = IO(
      assetPagesStore.addOne(assetPage)
    )

    def save(chapters: List[Chapter]): IO[Unit] = IO(
      chapterStore.addAll(chapters)
    )

    def findEnabledAssets(): IO[List[Asset]] = IO(
      assetsStore.values.filter(_.enabled == true).toList
    )

    def findAssetsPages(assets: List[Asset]): IO[List[AssetPage]] = IO {
      val assetIds = assets.map(_.id)

      assetPagesStore.filter(page => assetIds.contains(page.assetId)).toList
    }

    def findAssetsChapters(assets: List[Asset]): IO[List[AssetChapters]] = IO {
      assets.map { asset =>
        val chapters = chapterStore.filter(_.assetTitle == asset.name).toList
        AssetChapters(asset, chapters)
      }
    }
