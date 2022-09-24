package manggregator.modules.crawler.domain

import cats.effect._
import manggregator.modules.crawler.domain.Crawl.CrawlJob._
import manggregator.modules.crawler.domain.Asset._

trait SiteCrawler:
  def discoverTitles(job: DiscoverTitlesCrawlJob): IO[List[AssetSource]]

  def scrapeChapters(job: ScrapeChaptersCrawlJob): IO[List[Chapter]]
