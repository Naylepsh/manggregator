package crawler.services.site_crawlers.nyaa

import weaver.*
import sttp.client3.SttpBackend
import sttp.capabilities.WebSockets
import cats.effect.IO
import cats.effect.kernel.Resource
import crawler.resources.httpclient
import cats.*
import cats.implicits.*
import cats.syntax.all.*
import crawler.domain.Crawl.CrawlJob.ScrapeChaptersCrawlJob
import java.util.UUID
import core.Url

object NyaaCrawlerSuite extends IOSuite:
  override type Res = NyaaCrawler[IO]
  override def sharedResource: Resource[IO, Res] =
    val client = httpclient.makeClient
    client.map(_ => new NyaaCrawler(client))

  test(
    "Request for a specific title and translator group should return all releases"
  ) { crawler =>
    val allChaptersCount = 12
    for chapters <- crawler.scrapeChapters(scrapeJob)
    yield expect.all(
      chapters.isRight,
      chapters.map(_.length) == Right(allChaptersCount)
    )
  }

  private val scrapeJob = ScrapeChaptersCrawlJob(
    url = Url("https://nyaa.si/?f=0&c=1_2&q=%5BYameii%5D+Kinsou+no+Vermeil"),
    assetId = UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a")
  )
