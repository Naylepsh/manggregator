package library.services

import library.domain.ChapterRepository
import library.domain.Models.Chapter
import cats.effect.IO
import java.{util => ju}
import scala.collection.mutable.ListBuffer

object ChapterRepositoryImpl:
  val inMemoryRepository = new ChapterRepository:
    val store: ListBuffer[Chapter] = ListBuffer()

    override def save(chapters: List[Chapter]): IO[Unit] =
      IO.pure(store.addAll(chapters))

    override def findByAssetId(ids: List[ju.UUID]): IO[List[Chapter]] =
      IO.pure(store.filter(chapter => ids.contains(chapter.assetId)).toList)
