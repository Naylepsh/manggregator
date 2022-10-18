package http

import cats.data._
import cats.syntax.all._
import cats.effect._
import org.http4s.server.Router
import org.http4s.implicits._
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import crawler.http.Routes as CrawlerRoutes
import crawler.http.Endpoints as CrawlerEndpoints
import library.domain.AssetRepository
import org.http4s.HttpRoutes
import services.Builder
import org.http4s.dsl.io._
import org.http4s.implicits._

object Routes:
  // OpenAPI
  private val endpoints = CrawlerEndpoints.endpoints
  private val swaggerEndpoints =
    SwaggerInterpreter().fromEndpoints[IO](endpoints, "MANGgregAtor", "1.0")
  private val swaggerRoutes =
    Http4sServerInterpreter[IO]().toRoutes(swaggerEndpoints)

  def router(assetRepository: AssetRepository) =
    (crawlerRouter(assetRepository) <+> swaggerRoutes).orNotFound.onError(
      error => Kleisli { _ => IO.println(error) }
    )

  private def crawlerRouter(assetRepository: AssetRepository) =
    val library = Builder.library(assetRepository)
    val props = CrawlerRoutes.Props(library)

    CrawlerRoutes.routes(props)
