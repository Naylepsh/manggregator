package library.domain

import java.util.{ Date, UUID }

import scala.util.Try

import core.Newtype

import OrphanInstances.given

object chapter:
  import library.domain.asset.*

  type ChapterId = ChapterId.Type
  object ChapterId extends Newtype[UUID]:
    def apply(value: String): Either[String, ChapterId] =
      Try(UUID.fromString(value)).toEither
        .map(id => ChapterId(id))
        .left
        .map(_.toString)

  type ChapterNo = ChapterNo.Type
  object ChapterNo extends Newtype[String]

  type ChapterUrl = ChapterUrl.Type
  object ChapterUrl extends Newtype[String]

  type DateReleased = DateReleased.Type
  object DateReleased extends Newtype[Date]

  type Seen = Seen.Type
  object Seen extends Newtype[Boolean]

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
      assetName: ChapterNo
  )
