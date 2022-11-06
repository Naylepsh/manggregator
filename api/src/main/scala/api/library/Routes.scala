package api.library

import cats._
import cats.implicits._
import cats.syntax._
import cats.effect.kernel.Async
import scala.util.Try
import java.util.UUID
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import library.domain.asset._
import library.services._
import library.persistence.Storage
import api.library.responses._
import api.library.params._

object Routes:
  case class Props[F[_]](storage: Storage[F])

  def routes[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    getAssetChaptersRouter(props)
      <+> createAssetRouter(props)
      <+> createAssetPageRouter(props)

  def createAssetRouter[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      Endpoints.createAssetEndpoint.serverLogic(createAsset(props))
    )

  def createAssetPageRouter[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      Endpoints.createAssetPageEndpoint.serverLogic(createAssetPage(props))
    )

  def createAssetPage[F[_]: Monad](
      props: Props[F]
  )(
      assetId: UUID,
      page: CreateChaptersPageParam
  ): F[Either[String, CreateChaptersPageResponse]] =
    Pages
      .create(page.toDomain(assetId))
      .map(_.map(pageId => CreateChaptersPageResponse(pageId.value)))
      .run(props.storage.pages)

  def getAssetChaptersRouter[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      Endpoints.getAssetsChaptersEndpoint.serverLogic(getAssetsChapters(props))
    )

  private def createAsset[F[_]: Monad](
      props: Props[F]
  )(asset: CreateAssetParam): F[Either[String, CreateAssetResponse]] =
    Assets
      .create(asset.toDomain)
      .map(_.map(assetId => CreateAssetResponse(assetId.value)))
      .run(props.storage.assets)

  private def getAssetsChapters[F[_]: Monad](
      props: Props[F]
  )(ids: String): F[Either[String, List[Asset]]] =
    parseIds(ids) match {
      case Left(reason) =>
        reason.asLeft[List[Asset]].pure

      case Right(ids) =>
        Assets
          .findManyWithChapters(ids.map(AssetId.apply))
          .run(props.storage)
          .map(_.asRight[String])
    }

  private def parseIds(ids: String): Either[String, List[UUID]] =
    Try(ids.split(",").map(UUID.fromString).toList).toEither.left
      .map(_.toString)
