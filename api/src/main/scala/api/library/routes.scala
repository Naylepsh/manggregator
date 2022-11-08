package api.library

import cats._
import cats.data._
import cats.syntax._
import cats.implicits._
import cats.effect.kernel.Async
import scala.util.Try
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import library.domain.asset._
import library.services._
import library.persistence.Storage
import api.library.responses._
import api.library.params._
import api.utils.routes.combine

object routes:
  case class Props[F[_]](storage: Storage[F])

  def all[F[_]: Async](props: Props[F]): HttpRoutes[F] =
    NonEmptyList
      .of(createAsset, createAssetPage, getAssetChapters)
      .sequence
      .map(combine)
      .run(props)

  private def createAsset[F[_]: Async]: Reader[Props[F], HttpRoutes[F]] =
    Reader { props =>
      Http4sServerInterpreter[F]().toRoutes(
        endpoints.createAssetEndpoint.serverLogic((asset) =>
          Assets
            .create(asset.toDomain)
            .map(_.map(assetId => CreateAssetResponse(assetId.value)))
            .run(props.storage.assets)
        )
      )
    }

  private def createAssetPage[F[_]: Async]: Reader[Props[F], HttpRoutes[F]] =
    Reader { props =>
      Http4sServerInterpreter[F]().toRoutes(
        endpoints.createAssetPageEndpoint.serverLogic((assetId, page) =>
          Pages
            .create(page.toDomain(assetId))
            .map(_.map(pageId => CreateChaptersPageResponse(pageId.value)))
            .run(props.storage.pages)
        )
      )
    }

  private def getAssetChapters[F[_]: Async]: Reader[Props[F], HttpRoutes[F]] =
    Reader { props =>
      Http4sServerInterpreter[F]().toRoutes(
        endpoints.getAssetsChaptersEndpoint.serverLogic((ids) =>
          Assets
            .findManyWithChapters(ids.map(AssetId.apply))
            .run(props.storage)
            .map(_.asRight[String])
        )
      )
    }
