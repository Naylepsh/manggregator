package library.persistence

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.UUID

import scala.collection.mutable.ListBuffer

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
import library.domain.asset.AssetId
import library.domain.chapter._

trait Chapters[F[_]]:
  def create(chapters: List[CreateChapter]): F[List[ChapterId]]
  def findByAssetIds(ids: List[AssetId]): F[List[Chapter]]

object Chapters:
  import ChaptersSQL._

  def make[F[_]: Concurrent: UUIDGen: Functor]: Chapters[F] = new Chapters[F]:
    val store: ListBuffer[Chapter] = ListBuffer()

    override def create(chapters: List[CreateChapter]): F[List[ChapterId]] =
      chapters
        .map(chapter =>
          randomUUID[F].map(id =>
            Chapter(
              ChapterId(id),
              chapter.no,
              chapter.url,
              chapter.dateReleased,
              chapter.assetId
            )
          )
        )
        .sequence
        .map(store.addAll.andThen(_.map(_.id).toList))

    override def findByAssetIds(ids: List[AssetId]): F[List[Chapter]] =
      store.filter(chapter => ids.contains(chapter.assetId)).toList.pure

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
                dateReleased = (new Timestamp(
                  chapter.dateReleased.value.getTime()
                )).toString,
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
