package crawler.domain

import java.util.UUID

import cats.effect._
import core.Url
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Library.AssetToCrawl

trait Library[F[_]]:
  def getAssetsToCrawl(): F[List[AssetToCrawl]]
  def handleResult(result: SuccessfulResult): F[Unit]

object Library:
  case class AssetToCrawl(site: String, assetId: UUID, url: Url)
