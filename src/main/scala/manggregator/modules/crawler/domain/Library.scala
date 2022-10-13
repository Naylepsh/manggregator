package manggregator.modules.crawler.domain

import cats.effect._
import manggregator.modules.crawler.domain.Crawl.CrawlResult._
import manggregator.modules.shared.domain.ChapterCrawl

trait Library:
  def getAssetsToCrawl(): IO[List[ChapterCrawl]]
  def handleResult(result: Result): IO[Unit]
