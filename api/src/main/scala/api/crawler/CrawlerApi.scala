package api.crawler

import api.domain.Api
import api.crawler.Routes
import api.crawler.Endpoints
import cats._
import cats.effect._

object CrawlerApi:
  def apply[F[_]: Async: Parallel](props: Routes.Props[F]): Api[F] = new Api {
    val endpoints = Endpoints.endpoints

    val routes = Routes.routes(props)
  }
