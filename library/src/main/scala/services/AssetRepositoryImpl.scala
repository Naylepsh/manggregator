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

    def findByName(name: String): IO[Option[Asset]] = IO(
      assetsStore
        .find { case (_, asset) => asset.name == name }
        .map { case (_, asset) => asset }
    )

    def save(asset: Asset): IO[Unit] = IO(assetsStore.addOne(asset.id -> asset))

    def findEnabledAssets(): IO[List[Asset]] = IO(
      assetsStore.values.filter(_.enabled == true).toList
    )

    def findManyByIds(ids: List[UUID]): IO[List[Asset]] = IO(
      assetsStore.values.filter(asset => ids.contains(asset.id)).toList
    )
