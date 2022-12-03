package library.services

import library.domain.page._
import library.domain.asset.AssetId
import library.persistence.Storage
import cats._
import cats.data._
import cats.implicits._

trait Pages[F[_]]:
  def create(page: CreateChaptersPage): F[Either[PageAlreadyExists, PageId]]
  def findPagesOfEnabledAssets(): F[List[ChaptersPageToCheck]]

object Pages:
  def make[F[_]: Monad](storage: Storage[F]): Pages[F] = new Pages[F]:
    def create(
        page: CreateChaptersPage
    ): F[Either[PageAlreadyExists, PageId]] =
      storage.pages.findByUrl(page.url).flatMap {
        case Some(_) =>
          PageAlreadyExists(page.url).asLeft.pure

        case None =>
          storage.pages.create(page).map(_.asRight)
      }

    def findPagesOfEnabledAssets(): F[List[ChaptersPageToCheck]] =
      for {
        assets <- storage.assets.findEnabledAssets()
        pages <- storage.pages.findByAssetIds(assets.map(_.id))
      } yield pages.map { case ChaptersPage(_, assetId, site, url) =>
        ChaptersPageToCheck(site, url, assetId)
      }
