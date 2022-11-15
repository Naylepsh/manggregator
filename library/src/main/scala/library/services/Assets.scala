package library.services

import library.domain.asset._
import library.domain.alias._
import cats.implicits._
import cats._
import cats.data._
import library.persistence.Storage

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[Either[String, AssetId]]
  def findManyWithChapters(assetIds: List[AssetId]): F[List[Asset]]

object Assets:
  def make[F[_]: Monad](storage: Storage[F]): Assets[F] = new Assets[F]:
    def create(
        asset: CreateAsset
    ): F[Either[String, AssetId]] =
      storage.assets.findByName(asset.name).flatMap {
        case Some(_) =>
          s"Asset with name ${asset.name} already exists".asLeft[AssetId].pure

        case None =>
          storage.assets.create(asset).map(_.asRight[String])
      }

    def findManyWithChapters(
        assetIds: List[AssetId]
    ): F[List[Asset]] =
      for {
        assets <- NonEmptyList
          .fromList(assetIds)
          .fold(storage.assets.findAll())(storage.assets.findManyByIds)
        chapters <- storage.chapters.findByAssetId(assets.map(_.id))
      } yield bindChaptersToAssets(assets, chapters)
