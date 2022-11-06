package crawler.domain

import cats.effect._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Library.AssetToCrawl
import java.util.UUID

trait Library[F[_]]:
  def getAssetsToCrawl(): F[List[AssetToCrawl]]
  def handleResult(result: Result): F[Unit]

object Library:
  case class AssetToCrawl(site: String, assetId: UUID, url: String)
