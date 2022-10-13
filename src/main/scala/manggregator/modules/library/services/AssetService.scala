package manggregator.modules.library.services

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import manggregator.modules.library.domain.AssetRepository
import manggregator.modules.library.domain.Models._
import manggregator.modules.shared.domain.ChapterCrawl

object AssetService:
  def getAssetsToCrawl(): Reader[AssetRepository, IO[List[ChapterCrawl]]] =
    Reader { repository =>
      for {
        assets <- repository.findEnabledAssets()
        pages <- repository.findAssetsPages(assets)
      } yield pages.map { case AssetPage(_, asset, site, url) =>
        ChapterCrawl(site, asset.name, url)
      }
    }
