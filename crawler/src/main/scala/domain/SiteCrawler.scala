package crawler.domain

import cats.effect._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Asset._

trait SiteCrawler:
  def discoverTitles(
      job: DiscoverTitlesCrawlJob
  ): IO[Either[Throwable, List[AssetSource]]]

  def scrapeChapters(
      job: ScrapeChaptersCrawlJob
  ): IO[Either[Throwable, List[Chapter]]]
