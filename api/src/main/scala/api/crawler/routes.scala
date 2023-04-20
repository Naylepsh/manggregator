package api.crawler

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import cats.syntax.all.*
import crawler.domain.Library
import crawler.services.Crawler
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.server.Router
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object routes:
  case class Props[F[_]](library: Library[F], crawling: Crawler[F])

  def all[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    crawl(props)

  private def crawl[F[_]: Async](
      props: Props[F]
  ): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      endpoints.crawlEndpoint.serverLogic((arg: Unit) =>
        props.crawling.crawl().run(props.library).start *>
          "Crawling started successfully".asRight[String].pure
      )
    )
