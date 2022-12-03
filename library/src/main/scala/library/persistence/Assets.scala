package library.persistence

import java.util.UUID

import scala.collection.mutable.Map as MutableMap

import cats._
import cats.data._
import cats.effect._
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import cats.implicits._
import cats.syntax._
import doobie._
import doobie.implicits._
import doobie.util.query._
import library.domain.asset._

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[AssetId]
  def findAll(): F[List[Asset]]
  def findManyByIds(ids: List[AssetId]): F[List[Asset]]
  def findByName(name: AssetName): F[Option[Asset]]
  def findEnabledAssets(): F[List[Asset]]

object Assets:
  def make[F[_]: Concurrent: UUIDGen: Functor]: Assets[F] = new Assets[F]:
    val store: MutableMap[AssetId, Asset] = MutableMap()

    override def create(asset: CreateAsset): F[AssetId] =
      randomUUID[F].map { id =>
        val assetId = AssetId(id)
        store.addOne(assetId -> Asset(assetId, asset.name, asset.enabled))
        assetId
      }

    override def findAll(): F[List[Asset]] = store.values.toList.pure

    override def findManyByIds(ids: List[AssetId]): F[List[Asset]] =
      store.values.filter(asset => ids.contains(asset.id)).toList.pure

    override def findByName(name: AssetName): F[Option[Asset]] =
      store
        .find { case (_, asset) => asset.name == name }
        .map { case (_, asset) => asset }
        .pure

    override def findEnabledAssets(): F[List[Asset]] =
      store.values.filter(_.enabled.value).toList.pure

  def makeSQL[F[_]: MonadCancelThrow: UUIDGen](xa: Transactor[F]): Assets[F] =
    new Assets[F]:
      import AssetSQL._

      override def create(asset: CreateAsset): F[AssetId] =
        for
          id <- randomUUID
          _ <- insert(
            AssetRecord(
              id = id,
              name = asset.name.value,
              enabled = asset.enabled.value
            )
          ).run.transact(xa)
        yield AssetId(id)

      override def findManyByIds(ids: List[AssetId]): F[List[Asset]] =
        NonEmptyList.fromList(ids).fold(List.empty.pure) { ids =>
          selectByIds(ids.map(_.value))
            .to[List]
            .map(_.map(AssetRecord.toDomain))
            .transact(xa)
        }

      override def findByName(name: AssetName): F[Option[Asset]] =
        selectByName(name.value).option
          .map(_.map(AssetRecord.toDomain))
          .transact(xa)

      override def findEnabledAssets(): F[List[Asset]] =
        selectEnabled
          .to[List]
          .map(_.map(AssetRecord.toDomain))
          .transact(xa)

      override def findAll(): F[List[Asset]] =
        selectAll
          .to[List]
          .map(_.map(AssetRecord.toDomain))
          .transact(xa)

private object AssetSQL:
  import mappings.given

  case class AssetRecord(
      id: UUID,
      name: String,
      enabled: Boolean
  )
  object AssetRecord:
    def toDomain(model: AssetRecord): Asset =
      Asset(
        id = AssetId(model.id),
        name = AssetName(model.name),
        enabled = Enabled(model.enabled)
      )

  val selectAll: Query0[AssetRecord] =
    sql"""
      SELECT * FROM asset
    """.query[AssetRecord]

  val selectEnabled: Query0[AssetRecord] =
    sql"""
      SELECT * FROM asset
      WHERE enabled = 1
    """.query[AssetRecord]

  def selectByName(name: String): Query0[AssetRecord] =
    sql"""
      SELECT * FROM asset
      WHERE name = $name
    """.query[AssetRecord]

  def selectByIds(ids: NonEmptyList[UUID]): Query0[AssetRecord] =
    (
      sql"""
        SELECT * FROM asset
        WHERE """ ++ Fragments.in(fr"id", ids)
    ).query[AssetRecord]

  def insert(record: AssetRecord): Update0 =
    sql"""
      INSERT INTO asset (id, name, enabled)
      VALUES (${record.id}, ${record.name}, ${record.enabled})
    """.update
