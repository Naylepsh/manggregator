package crawler.domain

import java.util.UUID

import Asset._

object Crawl:
  sealed trait CrawlJob:
    val url: String

  object CrawlJob:
    case class ScrapeChaptersCrawlJob(url: String, assetId: UUID)
        extends CrawlJob
    case class DiscoverTitlesCrawlJob(url: String, keyword: String)
        extends CrawlJob

  case class SiteCrawlJob(label: String, job: CrawlJob)

  object CrawlResult:
    sealed trait Result
    case class ChapterResult(chapters: List[Chapter]) extends Result
    case class TitlesResult(titles: List[AssetSource]) extends Result

  type SiteCrawlersMapping[F[_]] = Map[String, SiteCrawler[F]]
