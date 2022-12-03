package api.library

import scala.util.Try

import api.library.params._
import api.library.responses._
import api.utils.routes.combine
import cats._
import cats.data._
import cats.effect.kernel.Async
import cats.implicits._
import cats.syntax._
import library.domain.asset._
import library.domain.page._
import library.persistence.Storage
import library.services._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object routes:
  case class Services[F[_]](assets: Assets[F], pages: Pages[F])

  def all[F[_]: Async](props: Services[F]): HttpRoutes[F] =
    NonEmptyList
      .of(createAsset, createAssetPage, getAssetChapters)
      .sequence
      .map(combine)
      .run(props)

  private def createAsset[F[_]: Async]: Reader[Services[F], HttpRoutes[F]] =
    Reader { props =>
      Http4sServerInterpreter[F]().toRoutes(
        endpoints.createAssetEndpoint.serverLogic((asset) =>
          props.assets
            .create(asset.toDomain)
            .map(_.map(assetId => CreateAssetResponse(assetId.value)).left.map {
              case AssetAlreadyExists(assetName) =>
                s"Asset with the name of $assetName already exists"
            })
        )
      )
    }

  private def createAssetPage[F[_]: Async]: Reader[Services[F], HttpRoutes[F]] =
    Reader { props =>
      Http4sServerInterpreter[F]().toRoutes(
        endpoints.createAssetPageEndpoint.serverLogic((assetId, page) =>
          props.pages
            .create(page.toDomain(assetId))
            .map(
              _.map(pageId => CreateChaptersPageResponse(pageId.value)).left
                .map { case PageAlreadyExists(url) =>
                  s"Page with the url of $url already exists"
                }
            )
        )
      )
    }

  private def getAssetChapters[F[_]: Async]
      : Reader[Services[F], HttpRoutes[F]] =
    Reader { props =>
      Http4sServerInterpreter[F]().toRoutes(
        endpoints.getAssetsChaptersEndpoint.serverLogic((ids) =>
          props.assets
            .findManyWithChapters(ids.map(AssetId.apply))
            .map(_.asRight[String])
        )
      )
    }
