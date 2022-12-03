package library.services

import library.domain.asset._
import library.domain.alias._
import cats.implicits._
import cats._
import cats.data._
import library.persistence.Storage

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[Either[AssetAlreadyExists, AssetId]]
  def findManyWithChapters(assetIds: List[AssetId]): F[List[Asset]]

object Assets:
  def make[F[_]: Monad](storage: Storage[F]): Assets[F] = new Assets[F]:
    def create(
        asset: CreateAsset
    ): F[Either[AssetAlreadyExists, AssetId]] =
      storage.assets.findByName(asset.name).flatMap {
        case Some(_) => AssetAlreadyExists(asset.name).asLeft.pure
        case None    => storage.assets.create(asset).map(_.asRight)
      }

    def findManyWithChapters(
        assetIds: List[AssetId]
    ): F[List[Asset]] =
      for
        assets <-
          if (assetIds.isEmpty) storage.assets.findAll()
          else storage.assets.findManyByIds(assetIds)
        chapters <- storage.chapters.findByAssetIds(assets.map(_.id))
      yield bindChaptersToAssets(assets, chapters)
