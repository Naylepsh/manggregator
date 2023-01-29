package library.persistence

import weaver._
import cats.implicits._
import cats.effect._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import library.resources.database._
import library.config.types._
import library.domain.asset._
import library.suite.DatabaseSuite

object AssetsSuite extends DatabaseSuite:
  override def maxParallelism = 1

  override type Res = HikariTransactor[IO]
  override def sharedResource: Resource[cats.effect.IO, Res] =
    databaseResource

  testWithCleanDb("Created assets can be found") { xa =>
    val repository = Assets.makeSQL(xa)

    val enabledAsset = CreateAsset(AssetName("enabled-asset"), Enabled(true))
    val disabledAsset = CreateAsset(AssetName("disabled-asset"), Enabled(false))

    for
      enabledAssetId <- repository.create(enabledAsset)
      disabledAssetId <- repository.create(disabledAsset)
      assets <- repository.findAll()
      enabledAssetFromDb = assets.map(_.id).find(_ == enabledAssetId)
      disabledAssetFromDb = assets.map(_.id).find(_ == disabledAssetId)
      enabledAssets <- repository.findEnabledAssets()
      enabledAssetsIds = enabledAssets.map(_.id)
    yield expect.all(
      enabledAssetFromDb.isDefined,
      disabledAssetFromDb.isDefined,
      enabledAssetsIds.contains(enabledAssetFromDb.get),
      !enabledAssetsIds.contains(disabledAssetFromDb.get)
    )
  }

  testWithCleanDb("Can update an existing asset") { xa =>
    val repository = Assets.makeSQL(xa)

    val enabledAsset = CreateAsset(AssetName("enabled-asset"), Enabled(true))
    val newAssetName = AssetName("updated-name")
    val newAssetEnabled = Enabled(false)

    for
      enabledAssetId <- repository.create(enabledAsset)
      assets <- repository.findAll()
      assetBefore <- repository.findByName(enabledAsset.name)
      _ <- assetBefore
        .map { asset =>
          repository.update(
            UpdateAsset(
              id = asset.id,
              name = newAssetName,
              enabled = newAssetEnabled
            )
          )
        }
        .getOrElse(IO.unit)
      assetAfter <- repository.findByName(newAssetName)
      allAssets <- repository.findAll()
    yield expect.all(
      assetBefore.isDefined,
      assetAfter.isDefined,
      assetAfter.map(_.enabled) == newAssetEnabled.some,
      assetAfter.map(_.name) == newAssetName.some,
      assetAfter.map(_.id) == assetBefore.map(_.id),
      allAssets.length == 1
    )
  }
