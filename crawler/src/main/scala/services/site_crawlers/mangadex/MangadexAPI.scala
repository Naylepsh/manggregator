package crawler.services.site_crawlers.mangadex

import crawler.domain.Url
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.model.Uri
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.client3.circe._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import cats.effect._
import entities._

trait MangadexAPI[F[_]]:
  def getManga(mangaId: String): F[Either[Throwable, GetMangaResponse]]

object MangadexAPI:
  def makeIO(
      httpClient: Resource[IO, SttpBackend[IO, WebSockets]]
  ): MangadexAPI[IO] = new MangadexAPI[IO]:
    override def getManga(
        mangaId: String
    ): IO[Either[Throwable, GetMangaResponse]] =
      httpClient.use { backend =>
        val url =
          uri"https://api.mangadex.org/manga/$mangaId/feed?order[chapter]=desc&translatedLanguage[]=en"
        val request =
          basicRequest.get(url).response(asJson[GetMangaResponse])
        backend.send(request).map(_.body)
      }

  def extractMangaIdFromAssetPageUrl(url: Url): Either[Throwable, String] =
    url.value match
      case shortAssetPagePattern(id) => Right(id)
      case fullAssetPagePattern(id)  => Right(id)
      case _ =>
        Left(new RuntimeException(s"Can't parse manga id from ${url.value}"))

  private val shortAssetPagePattern = ".*/title/(.)*".r
  private val fullAssetPagePattern = ".*/title/(.)*/.*".r
