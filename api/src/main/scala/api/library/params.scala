package api.library

import java.net.URI
import java.util.UUID

import library.domain.alias.*
import library.domain.asset.*
import library.domain.page.*

object params:

  // --- Create asset ---

  case class CreateAssetParam(
      name: String,
      enabled: Boolean
  ):
    def toDomain: CreateAsset = CreateAsset(
      AssetName(name),
      Enabled(enabled)
    )

  // TODO:
  // case class AliasParam(name: String):
  //   def toDomain: CreateAlias = CreateAlias(AliasName(name))

  // --- Create chapters page ---

  case class CreateChaptersPageParam(site: String, url: URI):
    def toDomain(assetId: UUID): CreateChaptersPage =
      CreateChaptersPage(Site(site), PageUrl(url.toString), AssetId(assetId))
