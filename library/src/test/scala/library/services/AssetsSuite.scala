package library.services

import cats._
import cats.data._
import cats.syntax._
import cats.implicits._
import library.domain.asset._
import library.persistence
import java.util.UUID

class AssetsSuite extends munit.FunSuite:
  import AssetsSuite._
  import common._

  test("Cant create the same asset twice") {
    val assetRepository: persistence.Assets[Id] = new persistence.Assets[Id]:

      override def create(asset: CreateAsset): Id[AssetId] = ???

      override def findAll(): Id[List[Asset]] = ???

      override def findManyByIds(ids: NonEmptyList[AssetId]): Id[List[Asset]] =
        ???

      override def findByName(name: AssetName): Id[Option[Asset]] =
        sampleAsset.some

      override def findEnabledAssets(): Id[List[Asset]] = ???
    val storage =
      persistence.Storage(
        assetRepository,
        uselessChaptersRepository,
        uselessPagesRepository
      )

    val result = Assets
      .make(storage)
      .create(CreateAsset(sampleAsset.name, sampleAsset.enabled))
    assert(result.isLeft, "Creating duplicate asset should end in failure")
  }

object AssetsSuite:
  val sampleAsset = Asset(
    AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a")),
    AssetName("Sample Asset"),
    Enabled(true)
  )
