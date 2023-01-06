package library.services

import cats._
import cats.data._
import cats.implicits._
import library.domain.alias._
import library.domain.asset._
import library.persistence.Storage
import library.domain.chapter.DateReleased

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[Either[AssetAlreadyExists, AssetId]]
  def findManyWithChapters(assetIds: List[AssetId]): F[List[Asset]]
  def findRecentReleases(minDate: DateReleased): F[List[Asset]]

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

    override def findRecentReleases(minDate: DateReleased): F[List[Asset]] =
      for
        chapters <- storage.chapters.findRecentReleases(minDate)
        assets <- storage.assets.findManyByIds(chapters.map(_.assetId))
      yield bindChaptersToAssets(assets, chapters)
