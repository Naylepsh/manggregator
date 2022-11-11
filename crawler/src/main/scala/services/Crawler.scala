package crawler.services

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Queue
import org.legogroup.woof.{given, *}
import org.legogroup.woof.Logger.withLogContext
import scala.concurrent.duration._
import scala.language.postfixOps
import crawler.domain._
import crawler.domain.Crawl._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.SiteCrawler
import crawler.services.site_crawlers.MangakakalotCrawler

trait Crawler[F[_]]:
  def crawl(): F[Unit]

object Crawler:
  def make[F[_]: Monad: Logger](
      crawlQueue: Queue[F, SiteCrawlJob],
      resultQueue: Queue[F, Result],
      siteCrawlersMappings: Map[String, SiteCrawler[F]]
  ): Crawler[F] = new Crawler[F] {
    override def crawl(): F[Unit] =
      for {
        _ <- Logger[F].debug(s"Trying to pick up a job")
        potentialJob <- crawlQueue.tryTake
        _ <- potentialJob.map(handleJob(_) *> crawl()).getOrElse(Monad[F].unit)
      } yield ()

    private def handleJob(job: SiteCrawlJob): F[Unit] =
      Logger[F].debug(s"Picked up ${job.toString}") *> execute(job).flatMap(
        _ match {
          case Left(reason)  => Logger[F].error(reason)
          case Right(result) => resultQueue.offer(result)
        }
      )

    private def execute(job: SiteCrawlJob) =
      siteCrawlersMappings
        .get(job.label)
        .toRight(s"Unregistered site crawler for label: ${job.label}")
        .traverse(crawler =>
          job.job match {
            case chapterJob @ ScrapeChaptersCrawlJob(_, _) =>
              crawler
                .scrapeChapters(chapterJob)
                .map(_.map(ChapterResult(_)).left.map(_.toString))

            case titleJob @ DiscoverTitlesCrawlJob(_, _) =>
              crawler
                .discoverTitles(titleJob)
                .map(_.map(TitlesResult(_)).left.map(_.toString))
          }
        )
        .map(_.flatten)
  }

  def makeCluster[F[_]: Monad: Logger: Parallel](
      crawlQueue: Queue[F, SiteCrawlJob],
      resultQueue: Queue[F, Result],
      siteCrawlersMappings: Map[String, SiteCrawler[F]],
      clusterSize: Int = 1
  ): Crawler[F] = new Crawler[F]:
    val size = if clusterSize > 0 then clusterSize else 1
    val crawlers = (1 to size)
      .map(id => (id, make[F](crawlQueue, resultQueue, siteCrawlersMappings)))
      .toList

    override def crawl(): F[Unit] =
      crawlers.parTraverse { case (id, crawler) =>
        crawler.crawl().withLogContext("crawler-id", id.toString)
      }.void
