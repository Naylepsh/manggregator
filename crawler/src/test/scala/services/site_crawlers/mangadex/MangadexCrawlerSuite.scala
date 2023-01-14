package crawler.services.site_crawlers.mangadex

import cats.Id
import crawler.services.site_crawlers.mangadex.entities.{
  Chapter,
  ChapterMetadata,
  GetMangaResponse
}
import cats.implicits._
import crawler.domain.Url
import crawler.domain.Crawl.CrawlJob.ScrapeChaptersCrawlJob
import java.util.UUID

object MangadexCrawlerSuite extends weaver.FunSuite:
  test("Chapter scraping: Chapter uses external url if available") {
    val api = testApi(customGetMangaResponse(externalUrl.some).pure)
    val crawler = new MangadexCrawler[Id](api)

    val result = crawler.scrapeChapters(job)

    expect.all(
      result.isRight,
      result.map(_.head.url.value) == externalUrl.asRight
    )
  }

  test(
    "Chapter scraping: Chapter defaults to mangadex url if no external url is available"
  ) {
    val api = testApi(customGetMangaResponse(noExternalUrl).pure)
    val crawler = new MangadexCrawler[Id](api)

    val result = crawler.scrapeChapters(job)

    expect.all(
      result.isRight,
      result.map(_.head.url.value) == crawler.chapterUrl(chapterId).asRight
    )
  }

  test("Chapter scraping: Fails if cannot extract id from job url") {
    val api = testApi(customGetMangaResponse(noExternalUrl).pure)
    val crawler = new MangadexCrawler[Id](api)

    val result = crawler.scrapeChapters(jobWithInvalidUrl)

    expect(result.isLeft)
  }

  test("Chapter scraping: Fails if cannot extract chapter url") {
    val api = testApi(customGetMangaResponse(invalidExternalUrl.some).pure)
    val crawler = new MangadexCrawler[Id](api)

    val result = crawler.scrapeChapters(job)

    expect(result.isLeft)
  }

  test("Chapter scraping: Fails if cannot extract chapter release date") {
    val api =
      testApi(customGetMangaResponse(createdAt = dateInInvalidFormat).pure)
    val crawler = new MangadexCrawler[Id](api)

    val result = crawler.scrapeChapters(job)

    expect(result.isLeft)
  }

  private val externalUrl = "https://foo.bar/baz"
  private val invalidExternalUrl = "not-a-valid-url"
  private val noExternalUrl = None
  private val dateInInvalidFormat = "20:40:00 14-01-2023"
  private val job = ScrapeChaptersCrawlJob(
    assetId = UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a"),
    url = Url("https://mangadex/title/my-test-title")
  )
  private val jobWithInvalidUrl = ScrapeChaptersCrawlJob(
    assetId = UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a"),
    url = Url("https://mangadex/not-a-valid-path/my-test-title")
  )
  private val chapterId = "chapter-id-1"

  private def customGetMangaResponse(
      url: Option[String] = None,
      createdAt: String = "2023-01-14T20:00:00"
  ): GetMangaResponse =
    GetMangaResponse(
      data = List(
        Chapter(
          id = chapterId,
          attributes = ChapterMetadata(
            chapter = "1",
            externalUrl = url,
            createdAt = createdAt
          )
        )
      )
    )

  private def testApi(
      getMangaResult: Either[Throwable, GetMangaResponse]
  ): MangadexAPI[Id] = new MangadexAPI[Id]:
    override def getManga(
        mangaId: String
    ): Id[Either[Throwable, GetMangaResponse]] = getMangaResult
