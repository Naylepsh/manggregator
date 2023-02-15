package library.domain

import java.util.UUID

import scala.util.control.NoStackTrace

import core.Newtype

object page:
  import library.domain.asset.AssetId

  type PageId = PageId.Type
  object PageId extends Newtype[UUID]

  type Site = Site.Type
  object Site extends Newtype[String]

  type PageUrl = PageUrl.Type
  object PageUrl extends Newtype[String]

  case class SearchPage(id: PageId, site: Site, url: PageUrl)
  case class ChaptersPage(
      id: PageId,
      assetId: AssetId,
      site: Site,
      url: PageUrl
  )

  case class ChaptersPageToCheck(site: Site, url: PageUrl, assetId: AssetId)

  case class CreateChaptersPage(site: Site, url: PageUrl, assetId: AssetId)

  // --- Errors ---
  case class PageAlreadyExists(url: PageUrl) extends NoStackTrace
