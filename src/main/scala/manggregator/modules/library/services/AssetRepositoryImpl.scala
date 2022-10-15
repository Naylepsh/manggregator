package manggregator.modules.library.services

import manggregator.modules.library.domain.AssetRepository
import manggregator.modules.library.domain.Models._
import cats.effect.IO
import scala.collection.mutable.Map as MutableMap
import scala.collection.mutable.ListBuffer
import java.util.UUID

object AssetRepositoryImpl:
  object AssetInMemoryRepository extends AssetRepository:
    val assets: MutableMap[UUID, Asset] = MutableMap()
    val assetPages: ListBuffer[AssetPage] = ListBuffer()
    val chapterStore: ListBuffer[Chapter] = ListBuffer()

    def findByName(name: String): IO[Option[Asset]] = IO(
      assets
        .find { case (_, asset) => asset.name == name }
        .map { case (_, asset) => asset }
    )

    def save(asset: Asset): IO[Unit] = IO(assets.addOne(asset.id -> asset))

    def save(assetPage: AssetPage): IO[Unit] = IO(assetPages.addOne(assetPage))

    def save(chapters: List[Chapter]): IO[Unit] = IO(
      chapterStore.addAll(chapters)
    )

    def findEnabledAssets(): IO[List[Asset]] = IO(
      assets.values.filter(_.enabled == true).toList
    )

    def findAssetsPages(assets: List[Asset]): IO[List[AssetPage]] = IO {
      val assetIds = assets.map(_.id)

      assetPages.filter(page => assetIds.contains(page.assetId)).toList
    }
