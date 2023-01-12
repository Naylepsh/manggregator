package crawler.domain

import java.util.UUID

import scala.util.control.NoStackTrace

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
    sealed trait SuccessfulResult
    case class ChapterResult(chapters: List[Chapter]) extends SuccessfulResult
    case class TitlesResult(titles: List[AssetSource]) extends SuccessfulResult

    case class CrawlError(url: String, reason: String) extends NoStackTrace

    type Result = Either[CrawlError, SuccessfulResult]

  type SiteCrawlersMapping[F[_]] = Map[String, SiteCrawler[F]]
