package crawler.services.site_crawlers.mangadex

object entities:
  case class ChapterMetadata(
      chapter: String,
      translatedLanguage: String,
      externalUrl: Option[String],
      createdAt: String
  )

  case class Chapter(id: String, attributes: ChapterMetadata)

  case class GetMangaResponse(result: String, data: List[Chapter])
