package crawler.services.site_crawlers.mangadex

import crawler.domain.Url
import cats._
import cats.implicits._

object MangadexAPISuite extends weaver.FunSuite:
  test("Can extract manga id from a valid short url") {
    val id = "04b6cb84-d641-490a-8141-5426b15cecb9"
    expect(
      MangadexAPI.extractMangaIdFromAssetPageUrl(
        Url(s"https://mangadex.org/title/$id")
      ) == id.pure
    )
  }

  test("Can extract manga id from a valid full url") {
    val id = "04b6cb84-d641-490a-8141-5426b15cecb9"
    expect(
      MangadexAPI.extractMangaIdFromAssetPageUrl(
        Url(s"https://mangadex.org/title/$id/the-manga-title-goes-here")
      ) == id.pure
    )
  }

  test("Can't extract manga id from a junk url") {
    val urls = List(
      "https://github.com/",
      "https://mangadex.org/chapter/7437257b-e3f6-462a-8178-7facb43b9729",
      "https://mangadex.org/"
    ).map(Url.apply)

    expect(
      urls
        .map(MangadexAPI.extractMangaIdFromAssetPageUrl)
        .filter(_.isLeft)
        .length == urls.length
    )
  }
