package library.services

import library.domain.page._
import library.domain.asset.AssetId
import library.persistence.Storage
import cats.data.Kleisli
import cats.implicits._
import cats._

trait Pages[F[_]]:
  def create(page: CreateChaptersPage): F[Either[String, PageId]]
  def findPagesOfEnabledAssets(): F[List[ChaptersPageToCheck]]

object Pages:
  def make[F[_]: Monad](storage: Storage[F]): Pages[F] = new Pages[F]:
    def create(
        page: CreateChaptersPage
    ): F[Either[String, PageId]] =
      storage.pages.findByUrl(page.url).flatMap {
        case Some(_) =>
          s"Asset page with url ${page.url} already exists"
            .asLeft[PageId]
            .pure

        case None =>
          storage.pages.create(page).map(_.asRight[String])
      }

    def findPagesOfEnabledAssets(): F[List[ChaptersPageToCheck]] =
      for {
        assets <- storage.assets.findEnabledAssets()
        pages <- storage.pages.findManyByAssetIds(assets.map(_.id))
      } yield pages.map { case ChaptersPage(_, assetId, site, url) =>
        ChaptersPageToCheck(site, url, assetId)
      }
