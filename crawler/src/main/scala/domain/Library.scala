package crawler.domain

import cats.effect._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Library.AssetToCrawl

trait Library:
  def getAssetsToCrawl(): IO[List[AssetToCrawl]]
  def handleResult(result: Result): IO[Unit]

object Library:
  case class AssetToCrawl(site: String, assetTitle: String, url: String)
