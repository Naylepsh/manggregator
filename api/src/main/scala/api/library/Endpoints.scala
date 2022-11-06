package api.library

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import api.utils.DateCodec.{encodeDate, decodeDate}
import api.library.responses._
import api.library.params._
import api.library.codecs.given
import library.domain.asset.Asset
import java.util.UUID
import sttp.tapir.codec.monix.newtype._

object Endpoints:
  val pathPrefix = "library"

  val getAssetsChaptersEndpoint
      : PublicEndpoint[String, String, List[Asset], Any] =
    endpoint.get
      .in(pathPrefix / "assets-chapters")
      .in(query[String]("ids"))
      .out(jsonBody[List[Asset]])
      .errorOut(stringBody)
      .description("Get the assets (by ids) and their chapters")

  val createAssetEndpoint
      : PublicEndpoint[CreateAssetParam, String, CreateAssetResponse, Any] =
    endpoint.post
      .in(pathPrefix / "assets")
      .in(jsonBody[CreateAssetParam])
      .out(jsonBody[CreateAssetResponse])
      .errorOut(stringBody)
      .description("Create an asset (manga, mahwa, etc.)")

  val createAssetPageEndpoint: PublicEndpoint[
    (UUID, CreateChaptersPageParam),
    String,
    CreateChaptersPageResponse,
    Any
  ] = endpoint.post
    .in(pathPrefix / "assets" / path[UUID]("assetId") / "pages")
    .in(jsonBody[CreateChaptersPageParam])
    .out(jsonBody[CreateChaptersPageResponse])
    .errorOut(stringBody)
    .description(
      "Add an asset page (a page where all chapters of given asset are available) to a related asset"
    )

  val endpoints = List(
    getAssetsChaptersEndpoint,
    createAssetEndpoint,
    createAssetPageEndpoint
  )
