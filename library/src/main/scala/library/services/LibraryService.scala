package library.services

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import library.domain.AssetRepository
import library.domain.ChapterRepository
import library.domain.PageRepository
import library.domain.Models._
import scala.collection.mutable.Map as MutableMap
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.Date

object LibraryService:
  case class Storage(
      assets: AssetRepository,
      pages: PageRepository,
      chapters: ChapterRepository
  )

  case class AssetToCrawl(site: String, url: String, assetId: UUID)

  case class AliasDTO(name: String)
  case class AssetPageDTO(site: String, url: String)
  object AssetPageDTO:
    def toDomainModel(id: UUID, assetId: UUID, page: AssetPageDTO): AssetPage =
      AssetPage(id, assetId, page.site, page.url)

  case class AssetDTO(
      name: String,
      enabled: Boolean,
      aliases: List[AliasDTO],
      titlePages: List[AssetPageDTO] = List()
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

  case class ChapterDTO(
      no: String,
      url: String,
      dateReleased: Date,
      assetId: UUID
  ):
    def ==(chapter: Chapter): Boolean =
      chapter.assetId == assetId && chapter.no == no

  object ChapterDTO:
    def toDomainModel(id: UUID, chapter: ChapterDTO): Chapter =
      Chapter(
        id,
        chapter.no,
        chapter.url,
        chapter.dateReleased,
        chapter.assetId
      )

    def filterNotContainedWithin(
        dtos: List[ChapterDTO],
        chapters: List[Chapter]
    ): List[ChapterDTO] =
      dtos.filter(chapter => chapters.find(chapter == _).isEmpty)

  def createAsset(
      asset: AssetDTO
  ): Kleisli[IO, Storage, Either[String, Asset]] =
    Kleisli { storage =>
      val id = randomUUID
      val domainModel = AssetDTO.toDomainModel(id, asset)

      storage.assets.findByName(domainModel.name).flatMap {
        case Some(_) =>
          IO.pure(
            s"Asset with name ${domainModel.name} already exists".asLeft[Asset]
          )

        case None =>
          val pages = asset.titlePages
            .map(page => AssetPageDTO.toDomainModel(randomUUID, id, page))
          storage.assets.save(domainModel).as(domainModel.asRight[String])
      }
    }

  def createAssetPage(
      page: AssetPageDTO,
      assetId: UUID
  ): Kleisli[IO, Storage, Either[String, AssetPage]] = Kleisli { storage =>
    val id = randomUUID
    val domainModel = AssetPageDTO.toDomainModel(id, assetId, page)

    storage.pages.findByUrl(domainModel.url).flatMap {
      case Some(_) =>
        IO.pure(
          s"Asset page with url ${domainModel.url} already exists"
            .asLeft[AssetPage]
        )

      case None =>
        storage.pages.save(domainModel).as(domainModel.asRight[String])
    }
  }

  def getAssetsToCrawl(): Kleisli[IO, Storage, List[AssetToCrawl]] =
    Kleisli { storage =>
      for {
        assets <- storage.assets.findEnabledAssets()
        pages <- storage.pages.findManyByAssetIds(assets.map(_.id))
      } yield pages.map { case AssetPage(_, assetId, site, url) =>
        AssetToCrawl(site, url, assetId)
      }
    }

  def getAssetsChapters(
      assetIds: List[UUID]
  ): Kleisli[IO, Storage, List[AssetChapters]] = Kleisli { storage =>
    for {
      assets <- storage.assets.findManyByIds(assetIds)
      chapters <- storage.chapters.findByAssetId(assets.map(_.id))
    } yield bindChaptersToAssets(assets, chapters)
  }

  def saveChapters(
      chapters: List[ChapterDTO]
  ): Kleisli[IO, Storage, Either[String, List[Chapter]]] = Kleisli { storage =>
    val assetIds = chapters.map(_.assetId)

    for {
      chaptersInStore <- storage.chapters.findByAssetId(assetIds)
      chaptersToSave = ChapterDTO
        .filterNotContainedWithin(
          chapters,
          chaptersInStore
        )
        .map(ChapterDTO.toDomainModel(randomUUID, _))
      _ <- storage.chapters.save(chaptersToSave)
    } yield chaptersToSave.asRight[String]
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
