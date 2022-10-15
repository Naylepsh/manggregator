package manggregator.modules.library.domain

import cats.effect._
import manggregator.modules.library.domain.Models._

trait AssetRepository:
  case class AssetChapters(asset: Asset, chapters: List[Chapter])

  def save(asset: Asset): IO[Unit]
  def save(assetPage: AssetPage): IO[Unit]
  def save(chapters: List[Chapter]): IO[Unit]

  def findByName(name: String): IO[Option[Asset]]
  def findEnabledAssets(): IO[List[Asset]]
  def findAssetsPages(assets: List[Asset]): IO[List[AssetPage]]
  def findAssetsChapters(assets: List[Asset]): IO[List[AssetChapters]]
