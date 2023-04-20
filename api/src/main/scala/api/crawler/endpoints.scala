package api.crawler

import sttp.tapir.*

object endpoints:
  val pathPrefix = "crawler"

  val crawlEndpoint: PublicEndpoint[Unit, String, String, Any] =
    endpoint.post
      .in(pathPrefix / "crawl")
      .out(stringBody)
      .errorOut(stringBody)
      .description("Start the crawl")

  val all = List(crawlEndpoint)
