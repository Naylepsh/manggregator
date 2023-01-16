package crawler.services.site_crawlers

import java.util.{Date, UUID}

import scala.util.Try
import scala.util.matching.Regex

import cats.effect._
import cats.implicits._
import com.github.nscala_time.time.Imports._
import crawler.domain.Asset._
import crawler.domain.Crawl.CrawlJob._
import crawler.domain.{SiteCrawler, Url}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.joda.time.DateTime
import retry.RetryPolicies._
import retry._

object MangakakalotCrawler extends SiteCrawler[IO]:
  /** Crawler for the following family of sites:
    *   - mangakakalot.com
    *   - readmangato.com
    */

  def parseTitles(content: String): List[AssetSource] = ???
  def discoverTitles(
      job: DiscoverTitlesCrawlJob
  ): IO[Either[Throwable, List[AssetSource]]] =
    getHtml(job.url).map(_.map(parseTitles))

  def scrapeChapters(
      job: ScrapeChaptersCrawlJob
  ): IO[Either[Throwable, List[Chapter]]] =
    Selectors.inferSelectors(job.url) match {
      case None =>
        IO(Left(RuntimeException(s"${job.url} has no registered selectors")))

      case Some(selectors) =>
        getHtml(job.url).map(
          _.flatMap(parseChapters(job.url, job.assetId, selectors))
        )
    }

  def parseChapters(url: Url, id: UUID, selectors: Selectors)(
      content: String
  ): Either[Throwable, List[Chapter]] =
    Try {
      (browser.parseString(content) >> elementList(selectors.chapterList))
        .flatMap { chapterElement =>
          for {
            nameElement <- chapterElement >?> element(selectors.chapterName)
            name = nameElement.text
            chapterRawUrl <- nameElement >?> attr("href")
            chapterUrl <- Url.fromString(chapterRawUrl).toOption
            no <- parseChapterNoFromName(name)
            timeUploaded <- chapterElement >?> allText(
              selectors.timeUploaded
            )
            dateReleased <- parseDateReleasedFromTimeUploaded(timeUploaded)
          } yield Chapter(
            assetId = id,
            no = no,
            url = chapterUrl,
            dateReleased = dateReleased
          )
        }
    }.toEither

  private def getHtml(url: Url): IO[Either[Throwable, String]] =
    /** The import has to stay here instead of being at the top level, due to
      * ambiguity of DurationInt and nscala_time implicits
      */
    import concurrent.duration.DurationInt

    /** Ideally retryingOnSomeErrors would be used, but figuring out what's a
      * reasonable error from JsoupBrowser is a bother...
      */
    retryingOnAllErrors(
      policy =
        limitRetries[IO](5) join exponentialBackoff[IO](100.milliseconds),
      onError = doNothing
    )(IO(browser.get(url.value).toHtml))
      .map(_.asRight[Throwable])
      .handleError(_.asLeft[String])

  private def doNothing(err: Throwable, details: RetryDetails): IO[Unit] =
    IO.unit

  private val chapterNoPattern = ".*Chapter ([0-9]+[.]?[0-9]?).*".r
  def parseChapterNoFromName(chapterName: String): Option[String] =
    chapterName match {
      case chapterNoPattern(no) => Some(no)
      case _                    => None
    }

  private val minutesAgoPattern = ".*([0-9]+) mins ago.*".r
  private val hoursAgoPattern = ".*([0-9]+) hour ago.*".r
  private val daysAgoPattern = ".*([0-9]+) day ago.*".r
  private val mangakakalotDatePattern =
    ".*([A-Za-z]{3})-([0-9]{2})-([0-9]{2}).*".r
  private val manganatoDatePattern =
    ".*([A-Za-z]{3}) ([0-9]{2}),([0-9]{2}).*".r
  def parseDateReleasedFromTimeUploaded(timeUploaded: String): Option[Date] =
    timeUploaded match {
      case minutesAgoPattern(minutes) =>
        Some((DateTime.now() - minutes.toInt.minutes).date)

      case hoursAgoPattern(hours) =>
        Some((DateTime.now() - hours.toInt.hours).date)

      case daysAgoPattern(days) =>
        Some((DateTime.now() - days.toInt.days).date)

      case mangakakalotDatePattern(month, day, year) =>
        composeDate(year, month, day)

      case manganatoDatePattern(month, day, year) =>
        composeDate(year, month, day)

      case _ => None
    }

  private def composeDate(
      year: String,
      month: String,
      day: String
  ): Option[Date] =
    for {
      m <- monthWordToNumeric(month)
      d <- day.toIntOption
      y <- year.toIntOption.map(_ + 2000)
    } yield (new DateTime())
      .withYear(y)
      .withMonthOfYear(m)
      .withDayOfMonth(d)
      .date

  private def monthWordToNumeric(monthWord: String): Option[Int] =
    List(
      "jan",
      "feb",
      "mar",
      "apr",
      "may",
      "jun",
      "jul",
      "aug",
      "sep",
      "oct",
      "nov",
      "dec"
    ).indexOf(monthWord.toLowerCase) match {
      case -1 => None
      case i  => Some(i + 1)
    }

  private val browser = JsoupBrowser()

  case class Selectors(
      chapterList: String,
      chapterName: String,
      timeUploaded: String
  )
  object Selectors:
    val mangakakalotSelectors = Selectors(
      chapterList = ".chapter-list .row",
      chapterName = "span:nth-of-type(1) a",
      timeUploaded = "span:nth-of-type(3)"
    )
    val manganatoSelectors = Selectors(
      chapterList = ".panel-story-chapter-list li",
      chapterName = "a",
      timeUploaded = "span:nth-of-type(2)"
    )

    private val mangakakalotUrlPattern = ".*mangakakalot.*".r
    private val manganatoUrlPattern = ".*manganato.*".r
    def inferSelectors(url: Url): Option[Selectors] = url.value match {
      case mangakakalotUrlPattern() => Some(mangakakalotSelectors)
      case manganatoUrlPattern()    => Some(manganatoSelectors)
      case _                        => None
    }
