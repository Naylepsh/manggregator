package library.domain

import cats.effect.IO
import library.domain.Models.AssetPage
import java.util.UUID

trait PageRepository:
  def save(page: AssetPage): IO[Unit]
  def save(pages: List[AssetPage]): IO[Unit]

  def findByUrl(url: String): IO[Option[AssetPage]]
  def findManyByAssetIds(assetIds: List[UUID]): IO[List[AssetPage]]
