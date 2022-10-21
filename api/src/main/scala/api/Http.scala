package api

import cats.data._
import cats.syntax.all._
import cats.effect._
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
import api.crawler.Routes as CrawlerRoutes
import api.library.LibraryApi
import api.library.Routes as LibraryRoutes
import _root_.crawler.domain.Library
import _root_.library.services.LibraryService.Storage

object Http:
  case class Docs(title: String, version: String)
  case class Props(docs: Docs, library: Library, storage: Storage)

  def apply(props: Props) =
    val crawlerProps = CrawlerRoutes.Props(props.library)
    val crawlerApi = CrawlerApi(crawlerProps)

    val libraryProps = LibraryRoutes.Props(props.storage)
    val libraryApi = LibraryApi(libraryProps)

    val endpoints = crawlerApi.endpoints <+> libraryApi.endpoints
    val openApiRoutes =
      createOpenApi(endpoints, props.docs.title, props.docs.version)

    val routes = crawlerApi.routes <+> libraryApi.routes

    val app = (routes <+> openApiRoutes).orNotFound.onError(error =>
      Kleisli { _ => IO.println(error) }
    )

    createServer(app)

  private def createOpenApi(
      endpoints: List[Endpoint[_, _, _, _, _]],
      title: String,
      version: String
  ): HttpRoutes[IO] =
    val swaggerEndpoints =
      SwaggerInterpreter().fromEndpoints[IO](endpoints, title, version)
    val swaggerRoutes =
      Http4sServerInterpreter[IO]().toRoutes(swaggerEndpoints)

    swaggerRoutes

  private def createServer(app: Kleisli[IO, Request[IO], Response[IO]]) =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(app)
      .build
      .use(_ => IO.never)
