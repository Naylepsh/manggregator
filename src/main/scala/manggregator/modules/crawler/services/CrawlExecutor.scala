package manggregator.modules.crawler.services

import cats._
import cats.implicits._
import cats.effect._
import manggregator.modules.crawler.domain.SiteCrawler
import manggregator.modules.crawler.domain.Crawl.SiteCrawlJob
import manggregator.modules.crawler.domain.Crawl.CrawlJob._

object CrawlExecutor:
  def crawl(siteCrawlersMappings: Map[String, SiteCrawler])(job: SiteCrawlJob) =
    siteCrawlersMappings
      .get(job.label)
      .toRight(s"Unregistered site crawler for label:${job.label}")
      .map(crawler =>
        job.job match {
          case chapterJob @ ScrapeChaptersCrawlJob(url, title) =>
            crawler.scrapeChapters(chapterJob)

          case titleJob @ DiscoverTitlesCrawlJob(_, _) =>
            crawler.discoverTitles(titleJob)
        }
      )
