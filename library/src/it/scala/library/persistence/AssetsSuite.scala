package library.persistence

import weaver._
import cats.implicits._
import cats.effect._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import library.resources.database._
import library.config.types._
import library.domain.asset._

object AssetsSuite extends IOSuite:

  override type Res = HikariTransactor[IO]
  override def sharedResource: Resource[cats.effect.IO, Res] =
    makeTransactorResource(
      DatabaseConfig(
        DatabasePath("./db-test.sqlite"),
        DatabaseUsername("username"),
        DatabasePassword("password")
      )
    ).evalTap(clearAssets)

  test("Created assets can be found") { xa =>
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

  private def clearAssets(xa: HikariTransactor[IO]) =
    sql"""
    DELETE FROM asset
    """.update.run.void.transact(xa)
