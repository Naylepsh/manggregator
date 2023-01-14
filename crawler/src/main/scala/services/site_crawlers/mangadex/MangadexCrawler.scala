package crawler.services.site_crawlers.mangadex

import crawler.domain.SiteCrawler
import crawler.resources.httpclient.HttpClient
import cats._
import cats.syntax.all._
import cats.effect._
import crawler.domain.Asset.{AssetSource, Chapter}
import crawler.domain.Crawl.CrawlJob.{
  DiscoverTitlesCrawlJob,
  ScrapeChaptersCrawlJob
}
import crawler.domain.Url
import java.util.{Date, UUID}
import org.joda.time.format.DateTimeFormat
import com.github.nscala_time.time.Imports._
import scala.util.Try

class MangadexCrawler[F[_]: Monad](
    api: MangadexAPI[F]
) extends SiteCrawler[F]:

  override def discoverTitles(
      job: DiscoverTitlesCrawlJob
  ): F[Either[Throwable, List[AssetSource]]] = ???

  override def scrapeChapters(
      job: ScrapeChaptersCrawlJob
  ): F[Either[Throwable, List[Chapter]]] =
    MangadexAPI.extractMangaIdFromAssetPageUrl(job.url) match
      case Left(reason) => reason.asLeft.pure
      case Right(mangaId) =>
        api
          .getManga(mangaId)
          .map(_.flatMap(_.data.traverse(toDomain(job.assetId, _))))

  def chapterUrl(chapterId: String): String =
    s"https://mangadex.org/chapter/${chapterId}"

  private def toDomain(
      assetId: UUID,
      chapter: entities.Chapter
  ): Either[Throwable, Chapter] =
    for
      url <- inferUrl(chapter)
      dateReleased <- parseDate(chapter.attributes.createdAt)
    yield Chapter(
      assetId,
      chapter.attributes.chapter,
      url,
      dateReleased
    )

  private def inferUrl(chapter: entities.Chapter): Either[Throwable, Url] =
    Url
      .fromString(
        chapter.attributes.externalUrl.getOrElse(chapterUrl(chapter.id))
      )
      .left
      .map(reason => new RuntimeException(reason))

  private val dateTimeFormat = DateTimeFormat.forPattern("Y-M-d'T'HH:mm:ss")
  private def parseDate(rawDate: String): Either[Throwable, Date] =
    Try(
      dateTimeFormat.parseDateTime(rawDate.split("[+]").head).date
    ).toEither

object MangadexCrawler:
  def makeIO(httpClient: HttpClient[IO]): MangadexCrawler[IO] =
    new MangadexCrawler[IO](MangadexAPI.makeIO(httpClient))