package crawler.services

import cats._
import cats.implicits._
import cats.effect._
import crawler.domain.SiteCrawler
import crawler.domain.Crawl.SiteCrawlJob
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Crawl.CrawlResult._

object CrawlExecutor:
  def crawl[F[_]: Functor](
      siteCrawlersMappings: Map[String, SiteCrawler[F]]
  )(job: SiteCrawlJob) =
    siteCrawlersMappings
      .get(job.label)
      .toRight(s"Unregistered site crawler for label:${job.label}")
      .map(crawler =>
        job.job match {
          case chapterJob @ ScrapeChaptersCrawlJob(_, _) =>
            crawler.scrapeChapters(chapterJob).map(_.map(ChapterResult(_)))

          case titleJob @ DiscoverTitlesCrawlJob(_, _) =>
            crawler.discoverTitles(titleJob).map(_.map(TitlesResult(_)))
        }
      )
