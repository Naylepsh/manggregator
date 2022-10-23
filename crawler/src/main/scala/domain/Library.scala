package crawler.domain

import cats.effect._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Library.AssetToCrawl
import java.util.UUID

trait Library:
  def getAssetsToCrawl(): IO[List[AssetToCrawl]]
  def handleResult(result: Result): IO[Unit]

object Library:
  case class AssetToCrawl(site: String, assetId: UUID, url: String)
