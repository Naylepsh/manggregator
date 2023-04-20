package library.domain

import java.util.UUID

import scala.collection.mutable.Map as MutableMap
import scala.util.control.NoStackTrace

import core.Newtype

object asset:
  import library.domain.alias.Alias
  import library.domain.alias.CreateAlias
  import library.domain.page.CreateChaptersPage
  import library.domain.chapter.Chapter

  type AssetId = AssetId.Type
  object AssetId extends Newtype[UUID]

  type AssetName = AssetName.Type
  object AssetName extends Newtype[String]

  type Enabled = Enabled.Type
  object Enabled extends Newtype[Boolean]

  case class Asset(
      id: AssetId,
      name: AssetName,
      enabled: Enabled,
      aliases: List[Alias] = List()
  ):
    def disable(): Asset =
      this.copy(enabled = Enabled(false))

    def enable(): Asset =
      this.copy(enabled = Enabled(true))

  // commands
  case class CreateAsset(
      name: AssetName,
      enabled: Enabled
  )

  case class UpdateAsset(
      id: AssetId,
      name: AssetName,
      enabled: Enabled
  )

  // reads
  case class AssetSummary(
      asset: Asset,
      chapters: List[Chapter]
  )
  object AssetSummary:
    def apply(
        assets: List[Asset],
        chapters: List[Chapter]
    ): List[AssetSummary] =
      val acc = MutableMap[AssetId, List[Chapter]]()

      chapters.foreach { chapter =>
        val otherChapters = acc.getOrElse(chapter.assetId, List())
        acc.addOne(chapter.assetId -> (chapter :: otherChapters))
      }

      assets.map { asset => AssetSummary(asset, acc.getOrElse(asset.id, List())) }

  // --- Errors ---
  case class AssetAlreadyExists(assetName: AssetName) extends NoStackTrace
  case class AssetDoesNotExist(assetName: AssetName)  extends NoStackTrace
