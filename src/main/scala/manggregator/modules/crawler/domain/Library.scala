package manggregator.modules.crawler.domain

import cats.effect._
import manggregator.modules.crawler.domain.Crawl.CrawlResult._

case class ChapterCrawl(site: String, assetTitle: String, url: String)

trait Library:
  def getAssetsToCrawl(): IO[List[ChapterCrawl]]
  def handleResult(result: Result): IO[Unit]
