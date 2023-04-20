package library.persistence

import java.util.UUID

import scala.collection.mutable.Map as MutableMap

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import cats.implicits.*
import cats.syntax.*
import doobie.*
import doobie.implicits.*
import doobie.util.query.*
import library.domain.asset.*

trait Assets[F[_]]:
  def create(asset: CreateAsset): F[AssetId]
  def update(asset: UpdateAsset): F[Unit]
  def findAll(): F[List[Asset]]
  def findManyByIds(ids: List[AssetId]): F[List[Asset]]
  def findByName(name: AssetName): F[Option[Asset]]
  def findEnabledAssets(): F[List[Asset]]

object Assets:
  def makeSQL[F[_]: MonadCancelThrow: UUIDGen](xa: Transactor[F]): Assets[F] =
    new Assets[F]:
      import AssetSQL.*

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

      override def update(asset: UpdateAsset): F[Unit] =
        AssetSQL
          .update(
            AssetRecord(
              id = asset.id.value,
              name = asset.name.value,
              enabled = asset.enabled.value
            )
          )
          .run
          .transact(xa)
          .void

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

  def update(record: AssetRecord): Update0 =
    sql"""
        UPDATE asset
        SET name = ${record.name}, enabled = ${record.enabled}
        WHERE id = ${record.id}
    """.update
