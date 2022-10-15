package manggregator.modules.crawler.domain

import cats.effect._
import manggregator.modules.crawler.domain.Crawl.CrawlResult._
import manggregator.modules.crawler.domain.Library.AssetToCrawl

trait Library:
  def getAssetsToCrawl(): IO[List[AssetToCrawl]]
  def handleResult(result: Result): IO[Unit]

object Library:
  case class AssetToCrawl(site: String, assetTitle: String, url: String)
