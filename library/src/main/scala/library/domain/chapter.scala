package library.domain

import java.util.{Date, UUID}

import scala.util.Try

import io.estatico.newtype.macros.newtype

object chapter:
  import library.domain.asset._

  @newtype
  case class ChapterId(value: UUID)
  object ChapterId:
    def apply(value: String): Either[String, ChapterId] =
      Try(UUID.fromString(value)).toEither
        .map(ChapterId.apply)
        .left
        .map(_.toString)

  @newtype
  case class ChapterNo(value: String)

  @newtype
  case class ChapterUrl(value: String)

  @newtype
  case class DateReleased(value: Date)

  @newtype
  case class Seen(value: Boolean)

  case class Chapter(
      id: ChapterId,
      no: ChapterNo,
      url: ChapterUrl,
      dateReleased: DateReleased,
      seen: Seen,
      assetId: AssetId
  ):
    def markAsSeen(): Chapter = this.copy(seen = Seen(true))

  // --- Commands ---
  case class CreateChapter(
      no: ChapterNo,
      url: ChapterUrl,
      dateReleased: DateReleased,
      assetId: AssetId
  ):
    val seen = Seen(false)

  object CreateChapter:
    def discardIfIn(
        all: List[CreateChapter],
        toDiscard: List[Chapter]
    ): List[CreateChapter] =
      all.filter(chapter =>
        toDiscard
          .find(other =>
            other.assetId == chapter.assetId && other.url == chapter.url
          )
          .isEmpty
      )

  case class CreateChaptersResult(
      created: List[ChapterId],
      alreadyExist: List[ChapterId]
  )

  //  --- Queries ---
  case class GetRecentChapters(
      minDate: DateReleased
  )
  case class RecentChapter(
      id: ChapterId,
      no: ChapterNo,
      url: ChapterUrl,
      assetName: AssetName
  )
