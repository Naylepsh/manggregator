package manggregator.modules.library.domain

object Models:
  case class Alias(id: Int, assetId: Int, name: String)
  case class Asset(
      id: Int,
      name: String,
      enabled: Boolean,
      aliases: List[Alias]
  )

  case class SearchPage(id: Int, site: String, url: String)
  case class AssetPage(id: Int, asset: Asset, site: String, url: String)
