package crawler.services.site_crawlers.nyaa

import java.util.{ Date, UUID }

import scala.util.Try

import cats.effect.kernel.{ Async, Resource }
import cats.implicits.*
import com.github.nscala_time.time.Imports.*
import core.Url
import crawler.domain.Asset.{ AssetSource, Chapter }
import crawler.domain.Crawl.CrawlJob.{ DiscoverTitlesCrawlJob, ScrapeChaptersCrawlJob }
import crawler.domain.SiteCrawler
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.model.Element
import org.joda.time.format.DateTimeFormat
import sttp.capabilities.WebSockets
import sttp.client3.{ SttpBackend, UriContext, basicRequest }

class NyaaCrawler[F[_]: Async](
    httpClient: Resource[F, SttpBackend[F, WebSockets]]
) extends SiteCrawler[F]:

  override def discoverTitles(
      job: DiscoverTitlesCrawlJob
  ): F[Either[Throwable, List[AssetSource]]] = ???

  override def scrapeChapters(
      job: ScrapeChaptersCrawlJob
  ): F[Either[Throwable, List[Chapter]]] =
    getReleasesPageContext(job.url).map { response =>
      for
        html     <- response.left.map(reason => new RuntimeException(reason))
        releases <- parseHtmlForReleases(html)
        chapters <- releases.traverse(_.toChapter(job.assetId))
      yield chapters
    }

  private def getReleasesPageContext(url: Url): F[Either[String, String]] =
    httpClient.use { backend =>
      basicRequest
        .get(uri"${url.value}")
        .send(backend)
        .map(_.body)
    }

  private def parseHtmlForReleases(html: String) =
    (browser.parseString(html) >> elementList("tr[class]"))
      .traverse(getRawRelease)

  private case class RawRelease(
      title: String,
      url: String,
      dateReleased: String
  ):
    def toChapter(assetId: UUID): Either[Throwable, Chapter] =
      for
        u <- Url
          .valid(url)
          .left
          .map(reason => new RuntimeException(reason))
        d <- parseDate(dateReleased)
      yield Chapter(
        assetId = assetId,
        /**
         * There's no good generic extractor for entry number, so storing a
         * full title will do
         */
        no = title,
        url = u,
        dateReleased = d
      )

  private def getRawRelease(
      releaseRow: Element
  ): Either[Throwable, RawRelease] =
    for
      nameColumn   <- getReleaseNameColumn(releaseRow)
      title        <- getTitle(nameColumn)
      url          <- getUrl(nameColumn)
      dateReleased <- getDateReleased(releaseRow)
    yield RawRelease(
      title = title,
      url = url,
      dateReleased = dateReleased
    )

  private def getReleaseNameColumn(
      releaseRow: Element
  ): Either[Throwable, Element] =
    (releaseRow >?> element(
      "td:nth-child(2) > a[href][title]:not([class])"
    )) match
      case None =>
        Left(
          new RuntimeException(
            "Could not extract a release name column from release row"
          )
        )
      case Some(value) => Right(value)

  private def getTitle(releaseNameColumn: Element): Either[Throwable, String] =
    (releaseNameColumn >?> text) match
      case None =>
        Left(
          new RuntimeException(
            "Could not extract a title from release name column"
          )
        )
      case Some(value) => Right(value)

  private def getUrl(releaseNameColumn: Element): Either[Throwable, String] =
    (releaseNameColumn >?> attr("href")) match
      case None =>
        Left(
          new RuntimeException(
            "Could not extract an href from release name column"
          )
        )
      case Some(relativePath) => Right(s"https://nyaa.si$relativePath")

  private def getDateReleased(releaseRow: Element): Either[Throwable, String] =
    (releaseRow >?> text("td[data-timestamp]")) match
      case None =>
        Left(
          new RuntimeException(
            "Could not extract a date released from release row"
          )
        )
      case Some(value) => Right(value)

  private val dateTimeFormat = DateTimeFormat.forPattern("Y-M-d HH:mm")
  private def parseDate(rawDate: String): Either[Throwable, Date] =
    Try(dateTimeFormat.parseDateTime(rawDate).date).toEither

  private val browser = JsoupBrowser()
