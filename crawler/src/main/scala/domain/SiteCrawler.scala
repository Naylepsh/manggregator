package crawler.domain

import cats.effect._
import crawler.domain.Asset._
import crawler.domain.Crawl.CrawlJob._

trait SiteCrawler[F[_]]:
  def discoverTitles(
      job: DiscoverTitlesCrawlJob
  ): F[Either[Throwable, List[AssetSource]]]

  def scrapeChapters(
      job: ScrapeChaptersCrawlJob
  ): F[Either[Throwable, List[Chapter]]]
