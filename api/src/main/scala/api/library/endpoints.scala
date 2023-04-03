package api.library

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import api.library.codecs.given
import api.library.params._
import api.library.responses._
import api.library.schemas.given
import api.utils.DateCodec.{decodeDate, encodeDate}
import cats.implicits._
import io.circe.generic.auto._
import library.domain.asset.{Asset, AssetSummary}
import sttp.model.StatusCode
import sttp.tapir.EndpointInput.Query
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object endpoints:
  private val pathPrefix = "library"

  private def stringToUuids(str: String): List[UUID] =
    str.split(",").map(UUID.fromString).toList
  private def optionalStringToUuids(str: Option[String]): List[UUID] =
    str.map(stringToUuids).getOrElse(List.empty)
  private def uuidsToString(uuids: List[UUID]): String =
    uuids.map(_.toString).mkString(",")

  private def uuidsQuery(name: String): Query[List[UUID]] =
    query[String](name).map(stringToUuids)(uuidsToString)
  private def optionalUuidsQuery(name: String): Query[List[UUID]] =
    query[Option[String]](name)
      .map(optionalStringToUuids)(uuidsToString(_).some)

  private val formatStr = "yyyy-MM-dd"
  private val format = new SimpleDateFormat(formatStr)
  private def dateQuery(date: String): Query[Date] =
    query[String](date).map(str => format.parse(str))(_.toString)

  val getAssetsChaptersEndpoint
      : PublicEndpoint[List[UUID], String, List[AssetSummaryResponse], Any] =
    endpoint.get
      .in(pathPrefix / "assets-chapters")
      .in(
        optionalUuidsQuery("ids").description("Comma separated list of UUIDs")
      )
      .out(jsonBody[List[AssetSummaryResponse]])
      .errorOut(stringBody)
      .description("Get the assets (by ids) and their chapters")

  val getAssetsWithRecentChapterReleasesEndpoint
      : PublicEndpoint[Date, String, List[AssetSummaryResponse], Any] =
    endpoint.get
      .in(pathPrefix / "recent-assets-chapters")
      .in(
        dateQuery("min-date").description(s"Date in $formatStr format")
      )
      .out(jsonBody[List[AssetSummaryResponse]])
      .errorOut(stringBody)
      .description("Get the assets with recently released chapters")

  val createAssetEndpoint
      : PublicEndpoint[CreateAssetParam, (StatusCode, String),  CreateAssetResponse, Any] =
    endpoint.post
      .in(pathPrefix / "assets")
      .in(jsonBody[CreateAssetParam])
      .out(jsonBody[CreateAssetResponse])
      .errorOut(statusCode)
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

  val all = List(
    getAssetsChaptersEndpoint,
    getAssetsWithRecentChapterReleasesEndpoint,
    createAssetEndpoint,
    createAssetPageEndpoint
  )
