package api.library

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import library.domain.Models.AssetChapters
import api.utils.DateCodec.{encodeDate, decodeDate}

object Endpoints:
  val pathPrefix = "library"

  val assetsChaptersEndpoint
      : PublicEndpoint[String, String, List[AssetChapters], Any] =
    endpoint.get
      .in(pathPrefix / "assets-chapters")
      .in(query[String]("ids"))
      .out(jsonBody[List[AssetChapters]])
      .errorOut(stringBody)
      .description("Get the assets (by ids) and their chapters")

  val endpoints = List(assetsChaptersEndpoint)
