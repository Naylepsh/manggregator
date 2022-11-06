package library.domain

import java.util.UUID
import java.util.Date
import monix.newtypes._

object chapter:
  import library.domain.asset.AssetId

  type ChapterId = ChapterId.Type
  object ChapterId extends NewtypeWrapped[UUID]

  type ChapterNo = ChapterNo.Type
  object ChapterNo extends NewtypeWrapped[String]

  type ChapterUrl = ChapterUrl.Type
  object ChapterUrl extends NewtypeWrapped[String]

  type DateReleased = DateReleased.Type
  object DateReleased extends NewtypeWrapped[Date]

  case class Chapter(
      id: ChapterId,
      no: ChapterNo,
      url: ChapterUrl,
      dateReleased: DateReleased,
      assetId: AssetId
  )

  case class CreateChapter(
      no: ChapterNo,
      url: ChapterUrl,
      dateReleased: DateReleased,
      assetId: AssetId
  )
  object CreateChapter:
    def discardIfIn(
        all: List[CreateChapter],
        toDiscard: List[Chapter]
    ): List[CreateChapter] =
      all.filter(chapter =>
        toDiscard
          .find(other =>
            other.assetId == chapter.assetId && other.no == chapter.no
          )
          .isEmpty
      )
