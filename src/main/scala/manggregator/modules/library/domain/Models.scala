package manggregator.modules.library.domain

import java.util.UUID

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

  case class Chapter(id: UUID, no: String, title: String, url: String)
