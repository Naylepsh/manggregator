package library.services

import library.domain.asset._
import library.domain.alias._
import library.persistence
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import cats.effect.std.UUIDGen.randomUUID
import cats._

object Assets:
  def create[F[_]: Monad](
      asset: CreateAsset
  ): Kleisli[F, persistence.Assets[F], Either[String, AssetId]] =
    Kleisli { assets =>
      assets.findByName(asset.name).flatMap {
        case Some(_) =>
          s"Asset with name ${asset.name} already exists".asLeft[AssetId].pure

        case None =>
          assets.create(asset).map(_.asRight[String])
      }
    }

  def findManyWithChapters[F[_]: FlatMap](
      assetIds: List[AssetId]
  ): Kleisli[F, persistence.Storage[F], List[Asset]] = Kleisli { storage =>
    for {
      assets <- storage.assets.findManyByIds(assetIds)
      chapters <- storage.chapters.findByAssetId(assets.map(_.id))
    } yield bindChaptersToAssets(assets, chapters)
  }
