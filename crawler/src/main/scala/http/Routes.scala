package crawler.http

import sttp.tapir._
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import cats.syntax.all._
import org.http4s.server.Router
import crawler.domain.Library
import cats.data.Reader
import crawler.services.CrawlingService

object Routes:
  case class Props(library: Library)

  def routes(props: Props): HttpRoutes[IO] = crawlRouter(props)

  private def crawl(props: Props)(x: Unit) =
    CrawlingService.crawl().run(props.library).parTraverse(x => x) *> IO.pure(
      Right[String, String]("Crawling started successfully")
    )

  private def crawlRouter(props: Props): HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.crawlEndpoint.serverLogic(crawl(props))
    )