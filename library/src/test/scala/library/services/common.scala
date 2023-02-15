package library.services

import cats._
import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import library.persistence
import library.domain.asset._
import library.domain.page._
import library.domain.chapter._

object common:
  class TestAssets[F[_]: Applicative: UUIDGen] extends persistence.Assets[F]:
    override def create(asset: CreateAsset): F[AssetId] =
      UUIDGen.randomUUID.map(id => AssetId(id))

    override def update(asset: UpdateAsset): F[Unit] = Applicative[F].unit

    override def findByName(name: AssetName): F[Option[Asset]] = None.pure

    override def findAll(): F[List[Asset]] = List.empty.pure

    override def findManyByIds(ids: List[AssetId]): F[List[Asset]] =
      List.empty.pure

    override def findEnabledAssets(): F[List[Asset]] = List.empty.pure

  class TestPages[F[_]: Applicative: UUIDGen] extends persistence.Pages[F]:
    override def create(page: CreateChaptersPage): F[PageId] =
      UUIDGen.randomUUID.map(id => PageId(id))

    override def findByUrl(url: PageUrl): F[Option[ChaptersPage]] = None.pure

    override def findByAssetIds(
        assetIds: List[AssetId]
    ): F[List[ChaptersPage]] = List.empty.pure

  class TestChapters[F[_]: Applicative] extends persistence.Chapters[F]:

    override def create(chapters: List[CreateChapter]): F[List[ChapterId]] =
      List.empty.pure

    override def markAsSeen(ids: List[ChapterId]): F[Unit] = Applicative[F].unit

    override def findByAssetIds(ids: List[AssetId]): F[List[Chapter]] =
      List.empty.pure

    override def findRecentReleases(
        minDateReleased: DateReleased
    ): F[List[Chapter]] = List.empty.pure
