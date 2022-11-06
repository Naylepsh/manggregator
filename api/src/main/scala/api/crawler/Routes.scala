package api.crawler

import sttp.tapir._
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import crawler.domain.Library
import crawler.services.Crawling

object Routes:
  case class Props[F[_]](library: Library[F], crawling: Crawling[F])

  def routes[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    crawlRouter(props)

  private def crawl[F[_]: Async](props: Props[F])(arg: Unit) =
    // TODO: Add background task / fire-and-forget
    props.crawling.crawl().run(props.library) *>
      "Crawling started successfully".asRight[String].pure

  private def crawlRouter[F[_]: Async](
      props: Props[F]
  ): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      Endpoints.crawlEndpoint.serverLogic(crawl(props))
    )
