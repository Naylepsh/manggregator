package crawler.domain

import java.util.UUID

import scala.util.control.NoStackTrace

import core.Url

import Asset.*

object Crawl:
  sealed trait CrawlJob:
    val url: Url

  object CrawlJob:
    case class ScrapeChaptersCrawlJob(url: Url, assetId: UUID)
        extends CrawlJob
    case class DiscoverTitlesCrawlJob(url: Url, keyword: String)
        extends CrawlJob

  case class SiteCrawlJob(label: String, job: CrawlJob)

  object CrawlResult:
    sealed trait SuccessfulResult
    case class ChapterResult(chapters: List[Chapter])  extends SuccessfulResult
    case class TitlesResult(titles: List[AssetSource]) extends SuccessfulResult

    case class CrawlError(url: Url, reason: String) extends NoStackTrace

    type Result = Either[CrawlError, SuccessfulResult]

  type SiteCrawlersMapping[F[_]] = Map[String, SiteCrawler[F]]
