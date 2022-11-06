package library.services

import cats._
import cats.implicits._
import cats.syntax._
import library.domain.asset._
import library.persistence
import java.util.UUID

class AssetsSuite extends munit.FunSuite:
  import AssetsSuite._

  test("Cant create the same asset twice") {
    val assets: persistence.Assets[Id] = new persistence.Assets[Id]:

      override def create(asset: CreateAsset): Id[AssetId] = ???

      override def findByName(name: AssetName): Id[Option[Asset]] =
        sampleAsset.some

      override def findManyByIds(ids: List[AssetId]): Id[List[Asset]] = ???

      override def findEnabledAssets(): Id[List[Asset]] = ???

    Assets
      .create[Id](
        CreateAsset(sampleAsset.name, sampleAsset.enabled)
      )
      .map(result =>
        assert(result.isLeft, "Creating duplicate asset should end in failure")
      )
      .run(assets)
  }

object AssetsSuite:
  val sampleAsset = Asset(
    AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a")),
    AssetName("Sample Asset"),
    Enabled(true)
  )
