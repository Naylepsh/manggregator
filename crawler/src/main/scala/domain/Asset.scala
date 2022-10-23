package crawler.domain

import java.util.Date
import java.util.UUID

object Asset:
  type Url = String

  case class AssetSource(title: String, url: Url)

  // asset can be a manga, manhwa, light novel, web novel, etc.
  case class Chapter(
      assetId: UUID,
      no: String,
      url: Url,
      dateReleased: Date
  )
