package crawler.services.site_crawlers

import munit.CatsEffectSuite
import java.util.Date
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeComparator
import java.util.UUID.randomUUID

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

  test("parsing chapter names extracts chapter number") {
    val inputs = List(
      ("Chapter 1", "1"),
      ("Chapter 1.5", "1.5"),
      ("Chapter 2: Hello, world!", "2")
    )

    inputs.foreach { case (chapterName, expectedChapterNo) =>
      val no = MangakakalotCrawler.parseChapterNoFromName(chapterName)
      assertEquals(no, Some(expectedChapterNo))
    }
  }

  test("parse chapters extracts chapters from HTML") {
    import MangakakalotCrawlerSuite._

    MangakakalotCrawler
      .parseChapters(
        url = "https://mangakakalot.com/manga/ot927321",
        id = randomUUID,
        selectors = Selectors.mangakakalotSelectors
      )(html)
      .foreach { chapters =>
        assertEquals(chapters.length, 4)
      }

  }

  test("parsing chapter name from gibberish data returns None") {
    assertEquals(MangakakalotCrawler.parseChapterNoFromName("gibberish"), None)
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

object MangakakalotCrawlerSuite:
  val html =
    """
    | <div class="chapter-list">
    |   <div class="row">
    |     <span><a href="https://mangakakalot.com/chapter/ot927321/chapter_2" title="Karami Zakari: Boku no Honto to Kimi no Uso Vol.5 Chapter 28">Vol.5 Chapter 28</a></span>
    |     <span>0</span>
    |     <span title="1 hour ago ">1 hour ago </span>
    |   </div>
    |   <div class="row">
    |     <span><a href="https://mangakakalot.com/chapter/ot927321/chapter_1.1" title="Karami Zakari: Boku no Honto to Kimi no Uso Vol.5 Chapter 27">Vol.5 Chapter 27</a></span>
    |     <span>4,065</span>
    |     <span title="Sep-25-2022 05:25">Sep-25-22</span>
    |   </div>
    |   <div class="row">
    |     <span><a href="https://mangakakalot.com/chapter/ot927321/chapter_1" title="Karami Zakari: Boku no Honto to Kimi no Uso Vol.1 Chapter 2">Vol.1 Chapter 2</a></span>
    |     <span>17,852</span>
    |     <span title="Nov-29-2021 11:01">Nov-29-21</span>
    |   </div>
    |   <div class="row">
    |     <span><a href="https://mangakakalot.com/chapter/ot927321/chapter_0" title="Karami Zakari: Boku no Honto to Kimi no Uso Chapter 1">Chapter 1</a></span>
    |     <span>30,958</span>
    |     <span title="Oct-14-2021 12:38">Oct-14-21</span>
    |   </div>
    | </div>
    """.stripMargin
