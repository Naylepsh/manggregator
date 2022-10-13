package manggregator.modules.library.services

import manggregator.modules.library.domain.AssetRepository
import manggregator.modules.library.domain.Models.Asset
import manggregator.modules.library.domain.Models.AssetPage
import cats.effect.IO
import scala.collection.mutable.Map as MutableMap
import scala.collection.mutable.ListBuffer

object AssetRepositoryImpl:
  object AssetInMemoryRepository extends AssetRepository:
    val assets: MutableMap[Int, Asset] = MutableMap()
    val assetPages: ListBuffer[AssetPage] = ListBuffer()

    def findByName(name: String): IO[Option[Asset]] = ???

    def save(asset: Asset): IO[Unit] = IO(assets.addOne(asset.id -> asset))

    def save(assetPage: AssetPage): IO[Unit] = IO(assetPages.addOne(assetPage))

    def findEnabledAssets(): IO[List[Asset]] = IO(
      assets.values.filter(_.enabled == true).toList
    )

    def findAssetsPages(assets: List[Asset]): IO[List[AssetPage]] = IO {
      val assetNames = assets.map(_.name)

      assetPages.filter(page => assetNames.contains(page.asset.name)).toList
    }
