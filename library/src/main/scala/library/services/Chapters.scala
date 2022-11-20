package library.services

import library.domain.chapter._
import cats._
import cats.implicits._
import cats.data.Kleisli
import library.persistence

trait Chapters[F[_]]:
  def create(chapters: List[CreateChapter]): F[CreateChaptersResult]

object Chapters:
  def make[F[_]: Monad](storage: persistence.Chapters[F]): Chapters[F] =
    new Chapters[F]:
      def create(
          chapters: List[CreateChapter]
      ): F[CreateChaptersResult] =
        for {
          chaptersInStore <- storage.findByAssetId(chapters.map(_.assetId))
          chaptersToSave = CreateChapter.discardIfIn(chapters, chaptersInStore)
          stored <- storage.create(chaptersToSave)
        } yield CreateChaptersResult(
          created = stored,
          alreadyExist = chaptersInStore.map(_.id)
        )
