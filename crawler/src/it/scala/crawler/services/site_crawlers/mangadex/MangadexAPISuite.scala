package crawler.services.site_crawlers.mangadex

import weaver._
import sttp.client3.SttpBackend
import sttp.capabilities.WebSockets
import cats.effect.IO
import cats.effect.kernel.Resource
import crawler.resources.httpclient
import cats._
import cats.implicits._
import cats.syntax.all._

object MangadexAPISuite extends IOSuite:

  override type Res = MangadexAPI[IO]
  override def sharedResource: Resource[IO, Res] =
    val client = httpclient.makeClient
    client.map(_ => MangadexAPI.make(client))

  test(
    "Request for a short manga should return all english-translated chapters"
  ) { api =>
    val allChaptersCount = 34
    for chapters <- api.getManga(
        "04b6cb84-d641-490a-8141-5426b15cecb9"
      )
    yield expect.all(
      chapters.isRight,
      chapters.map(_.data.length) == Right(allChaptersCount)
    )
  }
