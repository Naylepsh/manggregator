package manggregator.modules.crawler.domain.site_crawlers

import cats.effect._
import manggregator.modules.crawler.domain._
import manggregator.modules.crawler.domain.CrawlJob._

trait SiteCrawler:
  def discoverTitles(job: DiscoverTitlesCrawlJob): IO[List[AssetSource]]

  def scrapeChapters(job: ScrapeChaptersCrawlJob): IO[List[Chapter]]
