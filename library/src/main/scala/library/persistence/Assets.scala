package library.persistence

import library.domain.asset._
import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import scala.collection.mutable.Map as MutableMap
import java.util.UUID

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[AssetId]
  def findByName(name: AssetName): F[Option[Asset]]
  def findManyByIds(ids: List[AssetId]): F[List[Asset]]
  def findEnabledAssets(): F[List[Asset]]

object Assets:
  def make[F[_]: Concurrent: UUIDGen: Functor]: Assets[F] = new Assets[F]:
    val store: MutableMap[AssetId, Asset] = MutableMap()

    override def create(asset: CreateAsset): F[AssetId] =
      randomUUID[F].map { id =>
        val assetId = AssetId(id)
        store.addOne(assetId -> Asset(assetId, asset.name, asset.enabled))
        assetId
      }

    override def findByName(name: AssetName): F[Option[Asset]] =
      store
        .find { case (_, asset) => asset.name == name }
        .map { case (_, asset) => asset }
        .pure

    override def findManyByIds(ids: List[AssetId]): F[List[Asset]] =
      store.values.filter(asset => ids.contains(asset.id)).toList.pure

    override def findEnabledAssets(): F[List[Asset]] =
      store.values.filter(_.enabled.value).toList.pure