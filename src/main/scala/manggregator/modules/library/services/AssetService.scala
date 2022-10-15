package manggregator.modules.library.services

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import manggregator.modules.library.domain.AssetRepository
import manggregator.modules.library.domain.Models._

object AssetService:
  case class AssetToCrawl(site: String, url: String, title: String)

  def getAssetsToCrawl(): Reader[AssetRepository, IO[List[AssetToCrawl]]] =
    Reader { repository =>
      for {
        assets <- repository.findEnabledAssets()
        pages <- repository.findAssetsPages(assets)
      } yield pages.map { case AssetPage(_, assetId, site, url) =>
        AssetToCrawl(site, url, assets.find(_.id == assetId).get.name)
      }
    }
