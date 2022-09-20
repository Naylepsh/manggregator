package manggregator.modules.crawler.domain

import java.util.Date

// asset can be a manga, manhwa, light novel, web novel, etc.
case class Chapter(assetTitle: String, no: String, url: Url, dateReleased: Date)
