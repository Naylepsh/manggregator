package api.library

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import library.domain.Models.AssetChapters
import library.domain.Models.Asset
import api.utils.DateCodec.{encodeDate, decodeDate}
import library.services.LibraryService.AssetDTO

object Endpoints:
  val pathPrefix = "library"

  val getAssetsChaptersEndpoint
      : PublicEndpoint[String, String, List[AssetChapters], Any] =
    endpoint.get
      .in(pathPrefix / "assets-chapters")
      .in(query[String]("ids"))
      .out(jsonBody[List[AssetChapters]])
      .errorOut(stringBody)
      .description("Get the assets (by ids) and their chapters")

  val createAssetEndpoint: PublicEndpoint[AssetDTO, String, Asset, Any] =
    endpoint.post
      .in(pathPrefix / "assets")
      .in(jsonBody[AssetDTO])
      .out(jsonBody[Asset])
      .errorOut(stringBody)
      .description("Create an asset (manga, mahwa, etc.)")

  val endpoints = List(getAssetsChaptersEndpoint, createAssetEndpoint)
