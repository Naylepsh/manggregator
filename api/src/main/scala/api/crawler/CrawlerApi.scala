package api.crawler

import api.domain.Api
import api.crawler.Routes
import api.crawler.Endpoints

object CrawlerApi:
  def apply(props: Routes.Props): Api = new Api {
    val endpoints = Endpoints.endpoints

    val routes = Routes.routes(props)
  }
