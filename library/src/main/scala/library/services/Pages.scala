package library.services

import library.domain.page._
import library.domain.asset.AssetId
import library.persistence
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import cats._

object Pages:
  def create[F[_]: Monad](
      page: CreateChaptersPage
  ): Kleisli[F, persistence.Pages[F], Either[String, PageId]] = Kleisli {
    pages =>
      pages.findByUrl(page.url).flatMap {
        case Some(_) =>
          s"Asset page with url ${page.url} already exists"
            .asLeft[PageId]
            .pure

        case None =>
          pages.create(page).map(_.asRight[String])
      }
  }

  def findPagesToCheck[F[_]: FlatMap]()
      : Kleisli[F, persistence.Storage[F], List[ChaptersPageToCheck]] =
    Kleisli { storage =>
      for {
        assets <- storage.assets.findEnabledAssets()
        pages <- storage.pages.findManyByAssetIds(assets.map(_.id))
      } yield pages.map { case ChaptersPage(_, assetId, site, url) =>
        ChaptersPageToCheck(site, url, assetId)
      }
    }
