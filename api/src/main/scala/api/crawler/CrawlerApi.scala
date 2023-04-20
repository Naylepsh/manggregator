package api.crawler

import api.crawler.endpoints.all as allEndpoints
import api.crawler.routes.{ Props, all as allRoutes }
import api.domain.Api
import cats.*
import cats.effect.*

object CrawlerApi:
  def apply[F[_]: Async: Parallel](props: Props[F]): Api[F] = new Api:
    val endpoints = allEndpoints

    val routes = allRoutes(props)
