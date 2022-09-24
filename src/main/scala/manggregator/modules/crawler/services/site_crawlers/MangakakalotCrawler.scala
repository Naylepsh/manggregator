package manggregator.modules.crawler.services.site_crawlers

import cats.effect._
import scala.util.Try
import scala.util.matching.Regex
import manggregator.modules.crawler.domain.Crawl.CrawlJob._
import manggregator.modules.crawler.domain.Asset._
import manggregator.modules.crawler.domain.SiteCrawler
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import java.util.Date
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

object MangakakalotCrawler extends SiteCrawler:
  private val browser = JsoupBrowser()

  def getContent(url: Url): IO[Either[Throwable, String]] = IO {
    Try(browser.get(url).toHtml).toEither
  }

  def parseChapters(content: String): Either[Throwable, List[Chapter]] =
    Try {
      (browser.parseString(content) >> elementList(".chapter-list .row"))
        .flatMap { chapterElement =>
          for {
            name <- chapterElement >?> allText("span:nth-of-type(1)")
            no <- parseChapterNoFromName(name)
            timeUploaded <- chapterElement >?> allText("span:nth-of-type(3)")
            dateReleased <- parseDateReleasedFromTimeUploaded(timeUploaded)
          } yield Chapter(
            assetTitle = "TODO",
            no = no,
            url = "TODO",
            dateReleased = dateReleased
          )
        }
    }.toEither

  private val chapterNoPattern = ".*Chapter ([0-9]+.?[0-9]?).*".r
  def parseChapterNoFromName(chapterName: String): Option[String] =
    chapterName match {
      case chapterNoPattern(no) => Some(no)
      case _                    => None
    }

  private val hoursAgoPattern = ".*([0-9]+) hour ago.*".r
  private val daysAgoPattern = ".*([0-9]+) day ago.*".r
  private val datePattern = ".*([A-Za-z]{3})-([0-9]{2})-([0-9]{2}).*".r
  def parseDateReleasedFromTimeUploaded(timeUploaded: String): Option[Date] =
    timeUploaded match {
      case hoursAgoPattern(hours) =>
        Some((DateTime.now() + hours.toInt.hours).date)

      case daysAgoPattern(days) =>
        Some((DateTime.now() + days.toInt.days).date)

      case datePattern(month, day, year) =>
        for {
          m <- monthWordToNumeric(month)
          d <- day.toIntOption
          y <- year.toIntOption.map(_ + 2000)
        } yield (new DateTime())
          .withYear(y)
          .withMonthOfYear(m)
          .withDayOfMonth(d)
          .date

      case _ => None
    }

  def parseTitles(content: String): List[AssetSource] = ???
  def discoverTitles(job: DiscoverTitlesCrawlJob): IO[List[AssetSource]] =
    getContent(job.url).map(parseTitles)
  def scrapeChapters(job: ScrapeChaptersCrawlJob): IO[List[Chapter]] =
    getContent(job.url).map(parseChapters)

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
