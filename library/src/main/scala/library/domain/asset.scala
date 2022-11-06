package library.domain

import java.util.UUID
import scala.collection.mutable.Map as MutableMap
import monix.newtypes._

object asset:
  import library.domain.alias.Alias
  import library.domain.alias.CreateAlias
  import library.domain.page.CreateChaptersPage
  import library.domain.chapter.Chapter

  type AssetId = AssetId.Type
  object AssetId extends NewtypeWrapped[UUID]

  type AssetName = AssetName.Type
  object AssetName extends NewtypeWrapped[String]

  type Enabled = Enabled.Type
  object Enabled extends NewtypeWrapped[Boolean]

  case class Asset(
      id: AssetId,
      name: AssetName,
      enabled: Enabled,
      aliases: List[Alias] = List(),
      chapters: List[Chapter] = List()
  )

  def bindChaptersToAssets(
      assets: List[Asset],
      chapters: List[Chapter]
  ): List[Asset] =
    val acc = MutableMap[AssetId, List[Chapter]]()

    chapters.foreach { chapter =>
      val otherChapters = acc.getOrElse(chapter.assetId, List())
      acc.addOne(chapter.assetId -> (chapter :: otherChapters))
    }

    assets.map { asset =>
      asset.copy(chapters = acc.getOrElse(asset.id, List()))
    }

  case class CreateAsset(
      name: AssetName,
      enabled: Enabled
  )
