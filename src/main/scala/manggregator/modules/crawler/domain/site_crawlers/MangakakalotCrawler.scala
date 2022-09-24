package manggregator.modules.crawler.domain.site_crawlers

import cats.effect._
import manggregator.modules.crawler.domain.Crawl.CrawlJob._
import manggregator.modules.crawler.domain.Asset._

trait MangakakalotCrawler extends SiteCrawler:
  def getContent(url: Url): IO[String]
  def parseChapters(content: String): List[Chapter] = ???
  def parseTitles(content: String): List[AssetSource] = ???
  def discoverTitles(job: DiscoverTitlesCrawlJob): IO[List[AssetSource]] =
    getContent(job.url).map(parseTitles)
  def scrapeChapters(job: ScrapeChaptersCrawlJob): IO[List[Chapter]] =
    getContent(job.url).map(parseChapters)

// object MangakakalotCrawler extends SiteCrawler:
//   val crawler = new MangakakalotCrawler:
//     def getContent(url: Url): IO[String] = ???
