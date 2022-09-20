package manggregator.modules.crawler.domain

sealed trait CrawlJob:
  val url: String

object CrawlJob:
  case class ScrapeChaptersCrawlJob(url: String) extends CrawlJob
  case class DiscoverTitlesCrawlJob(url: String, keyword: String)
      extends CrawlJob
