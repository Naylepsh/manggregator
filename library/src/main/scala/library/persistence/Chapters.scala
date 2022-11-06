package library.persistence

import library.domain.chapter._
import library.domain.asset.AssetId
import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import scala.collection.mutable.ListBuffer

trait Chapters[F[_]]:
  def create(chapters: List[CreateChapter]): F[List[ChapterId]]
  def findByAssetId(ids: List[AssetId]): F[List[Chapter]]

object Chapters:
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

    override def findByAssetId(ids: List[AssetId]): F[List[Chapter]] =
      store.filter(chapter => ids.contains(chapter.assetId)).toList.pure
