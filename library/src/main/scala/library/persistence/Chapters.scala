package library.persistence

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{ Date, UUID }

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import cats.implicits.*
import cats.syntax.*
import doobie.*
import doobie.implicits.*
import doobie.util.query.*
import library.domain.asset.*
import library.domain.chapter.*

trait Chapters[F[_]]:
  def create(chapters: List[CreateChapter]): F[List[ChapterId]]
  def markAsSeen(ids: List[ChapterId]): F[Unit]
  def findByAssetIds(ids: List[AssetId]): F[List[Chapter]]
  def findRecentReleases(minDateReleased: DateReleased): F[List[Chapter]]

object Chapters:
  import ChaptersSQL.*

  def makeSQL[F[_]: MonadCancelThrow: UUIDGen](
      xa: Transactor[F]
  ): Chapters[F] =
    new Chapters[F]:

      override def create(chapters: List[CreateChapter]): F[List[ChapterId]] =
        for
          records <- chapters.traverse { chapter =>
            randomUUID.map { uuid =>
              ChapterRecord(
                id = uuid,
                no = chapter.no.value,
                url = chapter.url.value,
                dateReleased = dateToString(chapter.dateReleased.value),
                seen = chapter.seen.value,
                assetId = chapter.assetId.value
              )
            }
          }
          res <- insert.updateMany(records).transact(xa)
        yield records.map(chapter => ChapterId(chapter.id)).toList

      override def markAsSeen(ids: List[ChapterId]): F[Unit] =
        NonEmptyList.fromList(ids).fold(Applicative[F].unit) { ids =>
          ChaptersSQL.markAsSeen(ids.map(_.value)).run.void.transact(xa)
        }

      override def findByAssetIds(ids: List[AssetId]): F[List[Chapter]] =
        NonEmptyList.fromList(ids).fold(List.empty.pure) { ids =>
          selectByAssetIds(ids.map(_.value))
            .to[List]
            .map(_.map(ChapterRecord.toDomain))
            .transact(xa)
        }

      override def findRecentReleases(
          minDateReleased: DateReleased
      ): F[List[Chapter]] =
        selectRecentChapters(dateToString(minDateReleased.value))
          .to[List]
          .map(_.map(ChapterRecord.toDomain))
          .transact(xa)

      private def dateToString(date: Date): String =
        (new Timestamp(date.getTime())).toString

object ChaptersSQL:
  import mappings.given

  case class ChapterRecord(
      id: UUID,
      no: String,
      url: String,
      dateReleased: String,
      seen: Boolean,
      assetId: UUID
  )
  object ChapterRecord:
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS")

    def toDomain(record: ChapterRecord): Chapter =
      Chapter(
        id = ChapterId(record.id),
        no = ChapterNo(record.no),
        url = ChapterUrl(record.url),
        dateReleased = DateReleased(format.parse(record.dateReleased)),
        seen = Seen(record.seen),
        assetId = AssetId(record.assetId)
      )

  val insert = Update[ChapterRecord]("""
    INSERT INTO chapter (id, no, url, dateReleased, seen, assetId)
    VALUES (?, ?, ?, ?, ?, ?)
  """)

  def selectByAssetIds(assetIds: NonEmptyList[UUID]): Query0[ChapterRecord] =
    (
      sql"""
        SELECT id, no, url, dateReleased, seen, assetId FROM chapter
        WHERE """ ++ Fragments.in(fr"assetId", assetIds)
    ).query[ChapterRecord]

  def selectRecentChapters(
      minDateReleased: String
  ): Query0[ChapterRecord] =
    sql"""
        SELECT id, no, url, dateReleased, seen, assetId FROM chapter
        WHERE dateReleased >= $minDateReleased
    """.query

  def markAsSeen(ids: NonEmptyList[UUID]): Update0 =
    (sql"""
        UPDATE chapter
        SET seen = 1
        WHERE """ ++ Fragments.in(fr"id", ids)).update
