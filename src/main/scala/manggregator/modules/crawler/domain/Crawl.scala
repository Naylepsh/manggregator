package manggregator.modules.crawler.domain

import Asset._

object Crawl:
  sealed trait CrawlJob:
    val url: String

  object CrawlJob:
    case class ScrapeChaptersCrawlJob(url: String, assetTitle: String)
        extends CrawlJob
    case class DiscoverTitlesCrawlJob(url: String, keyword: String)
        extends CrawlJob

  case class SiteCrawlJob(label: String, job: CrawlJob)

  object CrawlResult:
    type TitlesResult = List[AssetSource]
    type ChapterResult = List[Chapter]
    type Result = TitlesResult | ChapterResult
