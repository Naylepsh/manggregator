package library.domain

import cats.effect.IO
import library.domain.Models.Chapter
import java.util.UUID

trait ChapterRepository:
  def save(chapters: List[Chapter]): IO[Unit]
  def findByAssetId(ids: List[UUID]): IO[List[Chapter]]
