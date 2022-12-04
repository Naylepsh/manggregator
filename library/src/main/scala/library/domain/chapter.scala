package library.domain

import java.util.{Date, UUID}

import io.estatico.newtype.macros.newtype

object chapter:
  import library.domain.asset.AssetId

  @newtype
  case class ChapterId(value: UUID)

  @newtype
  case class ChapterNo(value: String)

  @newtype
  case class ChapterUrl(value: String)

  @newtype
  case class DateReleased(value: Date)

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

  case class CreateChaptersResult(
      created: List[ChapterId],
      alreadyExist: List[ChapterId]
  )
