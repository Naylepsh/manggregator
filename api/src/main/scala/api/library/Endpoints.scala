package api.library

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import library.domain.Models.AssetChapters
import library.domain.Models.Asset
import api.utils.DateCodec.{encodeDate, decodeDate}
import library.services.LibraryService.AssetDTO
import library.services.LibraryService.AssetPageDTO
import library.domain.Models.AssetPage
import java.util.UUID

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

  val createAssetPageEndpoint: PublicEndpoint[
    (UUID, AssetPageDTO),
    String,
    AssetPage,
    Any
  ] = endpoint.post
    .in(pathPrefix / "assets" / path[UUID]("assetId") / "pages")
    .in(jsonBody[AssetPageDTO])
    .out(jsonBody[AssetPage])
    .errorOut(stringBody)
    .description(
      "Add an asset page (a page where all chapters of given asset are available) to a related asset"
    )

  val endpoints = List(
    getAssetsChaptersEndpoint,
    createAssetEndpoint,
    createAssetPageEndpoint
  )
