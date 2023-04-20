package library.services

import java.util.UUID

import cats.*
import cats.data.*
import cats.effect.IO
import cats.implicits.*
import cats.syntax.*
import library.domain.asset.*
import library.persistence
import library.services.common.TestAssets

class AssetsSuite extends munit.FunSuite:
  import AssetsSuite.*
  import common.*

  test("Cant create the same asset twice") {
    val storage =
      persistence.Storage(
        dataAssets(sampleAsset),
        new TestChapters,
        new TestPages
      )

    Assets
      .make(storage)
      .create(CreateAsset(sampleAsset.name, sampleAsset.enabled))
      .map(result =>
        assert(result.isLeft, "Creating duplicate asset should end in failure")
      )
  }

object AssetsSuite:
  val sampleAsset = Asset(
    AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a")),
    AssetName("Sample Asset"),
    Enabled(true)
  )

  def dataAssets(asset: Asset) = new TestAssets[IO]:
    override def findByName(name: AssetName): IO[Option[Asset]] =
      asset.some.pure
