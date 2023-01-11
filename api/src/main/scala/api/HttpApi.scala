package api

import _root_.crawler.domain.Library
import _root_.crawler.services.Crawler
import _root_.library.persistence.Storage
import api.config.Docs
import api.crawler.{CrawlerApi, routes => CrawlerRoutes}
import api.library.{LibraryApi, routes => LibraryRoutes}
import cats._
import cats.data._
import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import org.http4s._
import org.legogroup.woof.{_, given}
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object HttpApi:
  def make[F[_]: Async: Logger](
      docs: Docs,
      library: Library[F],
      crawler: Crawler[F],
      libraryServices: LibraryRoutes.Services[F]
  ): HttpApi[F] =
    new HttpApi[F](docs, library, crawler, libraryServices) {}

  private def createOpenApi[F[_]: Async](
      endpoints: List[Endpoint[_, _, _, _, _]],
      title: String,
      version: String
  ): HttpRoutes[F] =
    val swaggerEndpoints =
      SwaggerInterpreter().fromEndpoints[F](endpoints, title, version)
    val swaggerRoutes =
      Http4sServerInterpreter[F]().toRoutes(swaggerEndpoints)

    swaggerRoutes

sealed abstract class HttpApi[F[_]: Async: Logger] private (
    docs: Docs,
    library: Library[F],
    crawler: Crawler[F],
    libraryServices: LibraryRoutes.Services[F]
):
  import HttpApi._

  private val crawlerProps = CrawlerRoutes.Props(library, crawler)
  private val crawlerApi = CrawlerApi(crawlerProps)

  private val libraryApi = LibraryApi(libraryServices)

  private val endpoints = crawlerApi.endpoints <+> libraryApi.endpoints
  private val openApiRoutes =
    createOpenApi(endpoints, docs.title, docs.version)

  private val routes = crawlerApi.routes <+> libraryApi.routes <+> openApiRoutes

  val app: HttpApp[F] = routes.orNotFound.onError(error =>
    Kleisli { _ => Logger[F].error(error.toString) }
  )
