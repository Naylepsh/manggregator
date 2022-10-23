package services

import library.domain.PageRepository
import library.domain.Models.AssetPage
import cats.effect.IO
import scala.collection.mutable.ListBuffer
import java.util.UUID

object PageRepositoryImpl:
  object PageInMemoryRepositoryImpl extends PageRepository:
    val store: ListBuffer[AssetPage] = ListBuffer()

    def save(page: AssetPage): IO[Unit] =
      IO.pure(store.addOne(page))
    def save(pages: List[AssetPage]): IO[Unit] = IO.pure(store.addAll(pages))

    def findByUrl(url: String): IO[Option[AssetPage]] =
      IO.pure(store.find(_.url == url))

    def findManyByAssetIds(assetIds: List[UUID]): IO[List[AssetPage]] =
      IO.pure(store.filter(page => assetIds.contains(page.assetId)).toList)
