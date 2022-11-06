package library.persistence

import library.domain.page._
import library.domain.asset.AssetId
import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import scala.collection.mutable.ListBuffer

trait Pages[F[_]]:
  def create(page: CreateChaptersPage): F[PageId]
  // def createMany(pages: List[AssetPage]): IO[Unit]

  def findByUrl(url: PageUrl): F[Option[ChaptersPage]]
  def findManyByAssetIds(assetIds: List[AssetId]): F[List[ChaptersPage]]

object Pages:
  def make[F[_]: Concurrent: UUIDGen: Functor]: Pages[F] = new Pages[F]:
    val store: ListBuffer[ChaptersPage] = ListBuffer()

    override def create(page: CreateChaptersPage): F[PageId] =
      randomUUID[F].map(id =>
        val pageId = PageId(id)
        store.addOne(ChaptersPage(pageId, page.assetId, page.site, page.url))
        pageId
      )

    override def findByUrl(url: PageUrl): F[Option[ChaptersPage]] =
      store.find(_.url == url).pure

    override def findManyByAssetIds(
        assetIds: List[AssetId]
    ): F[List[ChaptersPage]] =
      store.filter(page => assetIds.contains(page.assetId)).toList.pure
