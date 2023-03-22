package crawler.services

import scala.concurrent.duration._
import scala.language.postfixOps

import cats._
import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Crawl._
import crawler.domain.{SiteCrawler, _}
import crawler.services.site_crawlers.MangakakalotCrawler
import org.legogroup.woof.Logger.withLogContext
import org.legogroup.woof.{_, given}

trait CrawlHandler[F[_]]:
  def crawl(): F[Unit]

object CrawlHandler:
  def make[F[_]: Monad: Logger](
      crawlQueue: Queue[F, SiteCrawlJob],
      resultQueue: Queue[F, Result],
      siteCrawlersMappings: Map[String, SiteCrawler[F]]
  ): CrawlHandler[F] = new CrawlHandler[F] {
    override def crawl(): F[Unit] =
      for
        _ <- Logger[F].debug(s"Trying to pick up a job")
        potentialJob <- crawlQueue.tryTake
        _ <- potentialJob.map(handleJob(_) *> crawl()).getOrElse(Monad[F].unit)
      yield ()

    private def handleJob(job: SiteCrawlJob): F[Unit] =
      Logger[F].debug(s"Picked up ${job.toString}") *> execute(job).flatMap(
        _ match
          case Left(reason) =>
            resultQueue.offer(CrawlError(job.job.url, reason).asLeft)

          case Right(result) => resultQueue.offer(result.asRight)
      )

    private def execute(job: SiteCrawlJob) =
      siteCrawlersMappings
        .get(job.label)
        .toRight(s"Unregistered site crawler for label: ${job.label}")
        .traverse(crawler =>
          job.job match
            case chapterJob @ ScrapeChaptersCrawlJob(_, _) =>
              crawler
                .scrapeChapters(chapterJob)
                .map(_.map(ChapterResult(_)).leftMap(_.toString))

            case titleJob @ DiscoverTitlesCrawlJob(_, _) =>
              crawler
                .discoverTitles(titleJob)
                .map(_.map(TitlesResult(_)).leftMap(_.toString))
        )
        .map(_.flatten)
  }

  def makeCluster[F[_]: Monad: Logger: Parallel](
      crawlQueue: Queue[F, SiteCrawlJob],
      resultQueue: Queue[F, Result],
      siteCrawlersMappings: Map[String, SiteCrawler[F]],
      clusterSize: Int = 1
  ): CrawlHandler[F] = new CrawlHandler[F]:
    val size = clusterSize.min(1)
    val crawlers = (1 to size)
      .map(id => (id, make[F](crawlQueue, resultQueue, siteCrawlersMappings)))
      .toList

    override def crawl(): F[Unit] =
      crawlers.parTraverse { case (id, crawler) =>
        crawler.crawl().withLogContext("crawler-id", id.toString)
      }.void
