package api.crawler

import sttp.tapir._

object Endpoints:
  val pathPrefix = "crawler"

  val crawlEndpoint: PublicEndpoint[Unit, String, String, Any] =
    endpoint.post
      .in(pathPrefix / "crawl")
      .out(stringBody)
      .errorOut(stringBody)
      .description("Start the crawl")

  val endpoints = List(crawlEndpoint)
