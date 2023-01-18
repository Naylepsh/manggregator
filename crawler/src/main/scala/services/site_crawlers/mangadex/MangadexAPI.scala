package crawler.services.site_crawlers.mangadex

import cats._
import cats.effect._
import cats.implicits._
import crawler.domain.Url
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.client3.circe._
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

import entities._

trait MangadexAPI[F[_]]:
  def getManga(mangaId: String): F[Either[Throwable, GetMangaResponse]]

object MangadexAPI:
  def make[F[_]: Async](
      httpClient: Resource[F, SttpBackend[F, WebSockets]]
  ): MangadexAPI[F] = new MangadexAPI[F]:
    override def getManga(
        mangaId: String
    ): F[Either[Throwable, GetMangaResponse]] =
      httpClient.use { backend =>
        val url =
          uri"https://api.mangadex.org/manga/$mangaId/feed?order[chapter]=desc&translatedLanguage[]=en"

        basicRequest
          .get(url)
          .response(asJson[GetMangaResponse])
          .send(backend)
          .map(_.body)
      }

  def extractMangaIdFromAssetPageUrl(url: Url): Either[Throwable, String] =
    url.value match
      case shortAssetPagePattern(id) => Right(id)
      case fullAssetPagePattern(id)  => Right(id)
      case _ =>
        Left(new RuntimeException(s"Can't parse manga id from ${url.value}"))

  private val shortAssetPagePattern = ".*/title/(.*)".r
  private val fullAssetPagePattern = ".*/title/(.*)/.*".r
