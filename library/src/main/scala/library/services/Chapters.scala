package library.services

import cats._
import cats.data._
import cats.implicits._
import library.domain.chapter._
import library.persistence

trait Chapters[F[_]]:
  def create(chapters: List[CreateChapter]): F[CreateChaptersResult]
  def markAsSeen(chapters: List[ChapterId]): F[Unit]

object Chapters:
  def make[F[_]: Monad](storage: persistence.Chapters[F]): Chapters[F] =
    new Chapters[F]:

      override def create(
          chapters: List[CreateChapter]
      ): F[CreateChaptersResult] =
        for {
          chaptersInStore <- storage.findByAssetIds(chapters.map(_.assetId))
          chaptersToSave = CreateChapter.discardIfIn(chapters, chaptersInStore)
          stored <- storage.create(chaptersToSave)
        } yield CreateChaptersResult(
          created = stored,
          alreadyExist = chaptersInStore.map(_.id)
        )

      override def markAsSeen(chapters: List[ChapterId]): F[Unit] =
        storage.markAsSeen(chapters)
