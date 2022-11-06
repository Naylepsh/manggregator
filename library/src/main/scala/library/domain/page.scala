package library.domain

import java.util.UUID
import monix.newtypes._

object page:
  import library.domain.asset.AssetId

  type PageId = PageId.Type
  object PageId extends NewtypeWrapped[UUID]

  type Site = Site.Type
  object Site extends NewtypeWrapped[String]

  type PageUrl = PageUrl.Type
  object PageUrl extends NewtypeWrapped[String]

  case class SearchPage(id: PageId, site: Site, url: PageUrl)
  case class ChaptersPage(id: PageId, assetId: AssetId, site: Site, url: PageUrl)

  case class ChaptersPageToCheck(site: Site, url: PageUrl, assetId: AssetId)

  case class CreateChaptersPage(site: Site, url: PageUrl, assetId: AssetId)
