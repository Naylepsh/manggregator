package library.services

import cats._
import cats.data._
import cats.implicits._
import library.domain.alias._
import library.domain.asset._
import library.domain.chapter.DateReleased
import library.persistence.Storage

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[Either[AssetAlreadyExists, AssetId]]
  def update(asset: UpdateAsset): F[Either[AssetDoesNotExist, Unit]]
  def findAll(): F[List[Asset]]
  def findManyWithChapters(assetIds: List[AssetId]): F[List[AssetSummary]]
  def findRecentReleases(minDate: DateReleased): F[List[AssetSummary]]

object Assets:
  def make[F[_]: Monad](storage: Storage[F]): Assets[F] = new Assets[F]:
    override def create(
        asset: CreateAsset
    ): F[Either[AssetAlreadyExists, AssetId]] =
      storage.assets.findByName(asset.name).flatMap {
        case Some(_) => AssetAlreadyExists(asset.name).asLeft.pure
        case None    => storage.assets.create(asset).map(_.asRight)
      }

    override def update(
        asset: UpdateAsset
    ): F[Either[AssetDoesNotExist, Unit]] =
      storage.assets.findByName(asset.name).flatMap {
        case Some(_) => storage.assets.update(asset).map(_.asRight)
        case None    => AssetDoesNotExist(asset.name).asLeft.pure
      }

    override def findAll(): F[List[Asset]] = storage.assets.findAll()

    override def findManyWithChapters(
        assetIds: List[AssetId]
    ): F[List[AssetSummary]] =
      for
        assets <-
          if (assetIds.isEmpty) storage.assets.findAll()
          else storage.assets.findManyByIds(assetIds)
        chapters <- storage.chapters.findByAssetIds(assets.map(_.id))
      yield AssetSummary(assets, chapters)

    override def findRecentReleases(
        minDate: DateReleased
    ): F[List[AssetSummary]] =
      for
        chapters <- storage.chapters.findRecentReleases(minDate)
        assets <- storage.assets.findManyByIds(chapters.map(_.assetId))
      yield AssetSummary(assets, chapters)
