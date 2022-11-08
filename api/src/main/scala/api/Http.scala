package api

import cats._
import cats.data._
import cats.syntax.all._
import cats.effect._
import cats.effect.implicits._
import cats.effect.std._
import com.comcast.ip4s._
import sttp.tapir.Endpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.ember.server._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import api.crawler.CrawlerApi
import api.crawler.routes as CrawlerRoutes
import api.library.LibraryApi
import api.library.routes as LibraryRoutes
import _root_.crawler.domain.Library
import _root_.library.persistence.Storage
import _root_.crawler.services.Crawling

object Http:
  case class Docs(title: String, version: String)
  case class Props[F[_]](
      docs: Docs,
      library: Library[F],
      storage: Storage[F],
      crawling: Crawling[F]
  )

  def apply[F[_]: Async: Console: Functor](props: Props[F]) =
    val crawlerProps = CrawlerRoutes.Props(props.library, props.crawling)
    val crawlerApi = CrawlerApi(crawlerProps)

    val libraryProps = LibraryRoutes.Props(props.storage)
    val libraryApi = LibraryApi(libraryProps)

    val endpoints = crawlerApi.endpoints <+> libraryApi.endpoints
    val openApiRoutes =
      createOpenApi(endpoints, props.docs.title, props.docs.version)

    val routes = crawlerApi.routes <+> libraryApi.routes <+> openApiRoutes

    val app = routes.orNotFound.onError(error =>
      Kleisli { _ => Console[F].println(error) }
    )

    createServer(app)

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

  private def createServer[F[_]: Async](
      app: Kleisli[F, Request[F], Response[F]]
  ) =
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(app)
      .build
      .use(_ => Async[F].never)
