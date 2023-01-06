package library.persistence

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.UUID

import cats._
import cats.data.NonEmptyList
import cats.effect._
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import cats.implicits._
import cats.syntax._
import doobie._
import doobie.implicits._
import doobie.util.query._
import library.domain.asset._
import library.domain.chapter._
import java.util.Date

trait Chapters[F[_]]:
  def create(chapters: List[CreateChapter]): F[List[ChapterId]]
  def findByAssetIds(ids: List[AssetId]): F[List[Chapter]]
  def findRecentReleases(minDateReleased: DateReleased): F[List[Chapter]]

object Chapters:
  import ChaptersSQL._

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
                assetId = chapter.assetId.value
              )
            }
          }
          res <- insert.updateMany(records).transact(xa)
        yield records.map(chapter => ChapterId(chapter.id)).toList

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
        assetId = AssetId(record.assetId)
      )

  val insert = Update[ChapterRecord]("""
    INSERT INTO chapter (id, no, url, dateReleased, assetId)
    VALUES (?, ?, ?, ?, ?)
  """)

  def selectByAssetIds(assetIds: NonEmptyList[UUID]): Query0[ChapterRecord] =
    (
      sql"""
        SELECT * FROM chapter
        WHERE """ ++ Fragments.in(fr"assetId", assetIds)
    ).query[ChapterRecord]

  def selectRecentChapters(
      minDateReleased: String
  ): Query0[ChapterRecord] =
    sql"""
        SELECT * FROM chapter
        WHERE dateReleased >= $minDateReleased
    """.query
