package crawler.domain

import java.util.Date

object Asset:
  type Url = String

  case class AssetSource(title: String, url: Url)

  // asset can be a manga, manhwa, light novel, web novel, etc.
  case class Chapter(
      assetTitle: String,
      no: String,
      url: Url,
      dateReleased: Date
  )
