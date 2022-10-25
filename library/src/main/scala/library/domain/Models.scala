package library.domain

import java.util.UUID
import java.util.Date

object Models:
  case class Alias(id: UUID, assetId: UUID, name: String)
  case class Asset(
      id: UUID,
      name: String,
      enabled: Boolean,
      aliases: List[Alias]
  )

  case class SearchPage(id: UUID, site: String, url: String)
  case class AssetPage(id: UUID, assetId: UUID, site: String, url: String)

  case class Chapter(
      id: UUID,
      no: String,
      url: String,
      dateReleased: Date,
      assetId: UUID
  )

  case class AssetChapters(asset: Asset, chapters: List[Chapter])
