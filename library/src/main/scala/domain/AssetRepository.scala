package library.domain

import cats.effect._
import library.domain.Models._
import java.util.UUID

trait AssetRepository:

  def save(asset: Asset): IO[Unit]

  def findByName(name: String): IO[Option[Asset]]
  def findManyByIds(ids: List[UUID]): IO[List[Asset]]
  def findEnabledAssets(): IO[List[Asset]]
