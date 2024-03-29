package crawler.domain

import java.util.UUID

import cats.effect.*
import core.Url
import crawler.domain.Crawl.CrawlResult.*
import crawler.domain.Library.AssetToCrawl

trait Library[F[_]]:
  def getAssetsToCrawl(): F[List[AssetToCrawl]]
  def handleResult(result: SuccessfulResult): F[Unit]

object Library:
  case class AssetToCrawl(site: String, assetId: UUID, url: Url)
