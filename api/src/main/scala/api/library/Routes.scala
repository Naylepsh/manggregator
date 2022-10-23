package api.library

import cats.effect.IO
import cats.implicits._
import cats.syntax._
import scala.util.Try
import java.util.UUID
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import library.domain.Models._
import library.services.LibraryService
import library.services.LibraryService._

object Routes:
  case class Props(storage: Storage)

  def routes(props: Props): HttpRoutes[IO] =
    getAssetChaptersRouter(props)
      <+> createAssetRouter(props)
      <+> createAssetPageRouter(props)

  def createAssetRouter(props: Props): HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.createAssetEndpoint.serverLogic(createAsset(props))
    )

  def createAssetPageRouter(props: Props): HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.createAssetPageEndpoint.serverLogic(createAssetPage(props))
    )

  def createAssetPage(
      props: Props
  )(assetId: UUID, page: AssetPageDTO): IO[Either[String, AssetPage]] =
    LibraryService.createAssetPage(page, assetId).run(props.storage.pages)

  def getAssetChaptersRouter(props: Props): HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.getAssetsChaptersEndpoint.serverLogic(getAssetsChapters(props))
    )

  private def createAsset(
      props: Props
  )(asset: AssetDTO): IO[Either[String, Asset]] =
    LibraryService
      .createAsset(asset)
      .run(props.storage.assets)

  private def getAssetsChapters(
      props: Props
  )(ids: String): IO[Either[String, List[AssetChapters]]] =
    parseIds(ids) match {
      case Left(reason) =>
        IO.pure(reason.asLeft[List[AssetChapters]])

      case Right(ids) =>
        LibraryService
          .getAssetsChapters(ids)
          .run(props.storage)
          .map(_.asRight[String])
    }

  private def parseIds(ids: String): Either[String, List[UUID]] =
    Try(ids.split(",").map(UUID.fromString).toList).toEither.left
      .map(_.toString)
