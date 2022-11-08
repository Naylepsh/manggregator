package api.crawler

import api.domain.Api
import api.crawler.routes.{Props, all as allRoutes}
import api.crawler.endpoints.all as allEndpoints
import cats._
import cats.effect._

object CrawlerApi:
  def apply[F[_]: Async: Parallel](props: Props[F]): Api[F] = new Api {
    val endpoints = allEndpoints

    val routes = allRoutes(props)
  }
