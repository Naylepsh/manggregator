package crawler.domain

import cats.effect.*
import crawler.domain.Asset.*
import crawler.domain.Crawl.CrawlJob.*

trait SiteCrawler[F[_]]:
  def discoverTitles(
      job: DiscoverTitlesCrawlJob
  ): F[Either[Throwable, List[AssetSource]]]

  def scrapeChapters(
      job: ScrapeChaptersCrawlJob
  ): F[Either[Throwable, List[Chapter]]]
