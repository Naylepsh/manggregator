package manggregator.modules.crawler.services.site_crawlers

import munit.CatsEffectSuite
import java.util.Date
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeComparator

class MangakakalotCrawlerSuite extends CatsEffectSuite:
  import MangakakalotCrawler._

  test(
    "parsing release date of chapter released minutes ago should return matching date"
  ) {
    assertDateReleasedParsingWorks(
      "1 mins ago",
      (DateTime.now() - 1.minutes).date
    )
  }

  test(
    "parsing release date of chapter released hours ago should return matching date"
  ) {
    assertDateReleasedParsingWorks(
      "2 hour ago",
      (DateTime.now() - 2.hours).date
    )
  }

  test(
    "parsing release date of chapter released days ago should return matching date"
  ) {
    assertDateReleasedParsingWorks(
      "3 day ago",
      (DateTime.now() - 3.days).date
    )
  }

  test(
    "parsing release date of chapter released on specified date in mangakakalot format should return matching date"
  ) {
    assertDateReleasedParsingWorks(
      "Aug-25-22",
      DateTime.parse("2022-08-25T00:00:00").date
    )
  }

  test(
    "parsing release date of chapter released on specified date in manganato format should return matching date"
  ) {
    assertDateReleasedParsingWorks(
      "Aug 25,22",
      (DateTime.parse("2022-08-25T00:00:00.959Z").date)
    )
  }

  test("parsing release date of gibberish returns None") {
    val result = MangakakalotCrawler.parseDateReleasedFromTimeUploaded(
      "gibberish-stuff here"
    )

    assertEquals(result, None)
  }

  private def assertDateReleasedParsingWorks(
      timeUploaded: String,
      expectedDate: Date
  ): Unit =
    val actualDate =
      MangakakalotCrawler.parseDateReleasedFromTimeUploaded(timeUploaded)

    /** Dates are not data classes, comparing them directly (even though their
      * string representation looks the same) will always fail. Thus, turning
      * them to strings.
      */
    assertEquals(
      DateTimeComparator
        .getDateOnlyInstance()
        .compare(actualDate.get, expectedDate),
      0
    )
