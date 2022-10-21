package library.services

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import library.domain.AssetRepository
import library.domain.ChapterRepository
import library.domain.Models._
import scala.collection.mutable.Map as MutableMap
import java.util.UUID

object LibraryService:
  case class AssetToCrawl(site: String, url: String, assetId: UUID)
  case class Storage(assets: AssetRepository, chapters: ChapterRepository)

  def getAssetsToCrawl(): Reader[AssetRepository, IO[List[AssetToCrawl]]] =
    Reader { repository =>
      for {
        assets <- repository.findEnabledAssets()
        pages <- repository.findAssetsPages(assets)
      } yield pages.map { case AssetPage(_, assetId, site, url) =>
        AssetToCrawl(site, url, assetId)
      }
    }

  def getAssetsChapters(
      assetIds: List[UUID]
  ): Reader[Storage, IO[List[AssetChapters]]] = Reader { storage =>
    for {
      assets <- storage.assets.findManyByIds(assetIds)
      chapters <- storage.chapters.findByAssetId(assets.map(_.id))
    } yield bindChaptersToAssets(assets, chapters)
  }

  private def bindChaptersToAssets(
      assets: List[Asset],
      chapters: List[Chapter]
  ): List[AssetChapters] =
    val acc = MutableMap[UUID, List[Chapter]]()

    chapters.foreach { chapter =>
      val otherChapters = acc.getOrElse(chapter.assetId, List())
      acc.addOne(chapter.assetId -> (chapter :: otherChapters))
    }

    assets.map { asset =>
      AssetChapters(asset, acc.getOrElse(asset.id, List()))
    }
