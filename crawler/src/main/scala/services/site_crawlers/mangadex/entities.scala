package crawler.services.site_crawlers.mangadex

object entities:
  case class ChapterMetadata(
      chapter: String,
      externalUrl: Option[String],
      createdAt: String
  )

  case class Chapter(id: String, attributes: ChapterMetadata)

  case class GetMangaResponse(data: List[Chapter])
