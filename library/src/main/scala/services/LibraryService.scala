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
import java.util.UUID.randomUUID

object LibraryService:
  case class AssetToCrawl(site: String, url: String, assetId: UUID)
  case class Storage(assets: AssetRepository, chapters: ChapterRepository)
  case class AliasDTO(name: String)
  case class AssetDTO(
      name: String,
      enabled: Boolean,
      aliases: List[AliasDTO]
  )
  object AssetDTO:
    def toDomainModel(id: UUID, asset: AssetDTO): Asset =
      Asset(
        id,
        asset.name,
        asset.enabled,
        asset.aliases.map(alias =>
          Alias(id = randomUUID, assetId = id, name = alias.name)
        )
      )

  def createAsset(
      asset: AssetDTO
  ): Reader[AssetRepository, IO[Either[String, Asset]]] =
    Reader { repository =>
      val id = randomUUID
      val domainModel = AssetDTO.toDomainModel(id, asset)

      repository.findByName(domainModel.name).flatMap {
        case Some(_) =>
          IO.pure(
            s"Asset with name ${domainModel.name} already exists".asLeft[Asset]
          )

        case None =>
          repository.save(domainModel).as(domainModel.asRight[String])
      }

    }

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
