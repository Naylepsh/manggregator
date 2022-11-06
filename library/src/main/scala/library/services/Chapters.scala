package library.services

import library.domain.chapter._
import library.persistence
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import cats._

object Chapters:
  def create[F[_]: FlatMap](
      chapters: List[CreateChapter]
  ): Kleisli[F, persistence.Chapters[F], Either[String, List[ChapterId]]] =
    Kleisli { storage =>
      for {
        chaptersInStore <- storage.findByAssetId(chapters.map(_.assetId))
        chaptersToSave = CreateChapter.discardIfIn(chapters, chaptersInStore)
        stored <- storage.create(chaptersToSave)
      } yield stored.asRight[String]
    }
