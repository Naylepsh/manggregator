package library.domain

import java.util.UUID

import scala.util.control.NoStackTrace

import io.estatico.newtype.macros.newtype

object page:
  import library.domain.asset.AssetId

  @newtype
  case class PageId(value: UUID)

  @newtype
  case class Site(value: String)

  @newtype
  case class PageUrl(value: String)

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
