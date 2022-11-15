package library.services

import cats.Id
import cats.data.NonEmptyList
import library.persistence
import library.domain.asset._
import library.domain.page._
import library.domain.chapter._

object common:
  val uselessAssetsRepository = new persistence.Assets[Id] {

    override def findAll(): Id[List[Asset]] = ???

    override def create(asset: CreateAsset): Id[AssetId] = ???

    override def findEnabledAssets(): Id[List[Asset]] = ???

    override def findByName(name: AssetName): Id[Option[Asset]] = ???

    override def findManyByIds(ids: NonEmptyList[AssetId]): Id[List[Asset]] =
      ???

  }

  val uselessPagesRepository = new persistence.Pages[Id] {

    override def create(page: CreateChaptersPage): Id[PageId] = ???

    override def findByUrl(url: PageUrl): Id[Option[ChaptersPage]] = ???

    override def findManyByAssetIds(
        assetIds: List[AssetId]
    ): Id[List[ChaptersPage]] = ???

  }

  val uselessChaptersRepository: persistence.Chapters[Id] =
    new persistence.Chapters[Id] {

      override def create(chapters: List[CreateChapter]): Id[List[ChapterId]] =
        ???

      override def findByAssetId(ids: List[AssetId]): Id[List[Chapter]] = ???

    }
