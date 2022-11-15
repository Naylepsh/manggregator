package library.services

import library.domain.chapter._
import library.domain.asset._
import library.persistence
import common._
import cats._
import cats.implicits._
import cats.effect._
import java.util.UUID
import java.util.Date
import java.time.Instant

class ChaptersSuite extends munit.FunSuite:
  import ChaptersSuite._

  test("Cant create the same chapter twice") {
    val newChapters = (1 to 3)
      .map(i =>
        CreateChapter(
          ChapterNo(i.toString),
          ChapterUrl(s"http://foo.bar/asset/${i}"),
          DateReleased(
            Date.from(Instant.parse("2022-11-06T12:52:54.966933505Z"))
          ),
          AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28a"))
        )
      )
      .toList
    val chapters = sampleChapter.toCreate :: newChapters

    Chapters
      .make(dataChapters(List(sampleChapter)))
      .create(chapters)
      .map(_.map(ids => assertEquals(ids.length, 0)))
  }

object ChaptersSuite:
  val sampleChapter = Chapter(
    ChapterId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28c")),
    ChapterNo("42"),
    ChapterUrl("http://foo.bar/asset/42"),
    DateReleased(
      Date.from(Instant.parse("2022-11-06T12:52:54.966933505Z"))
    ),
    AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28b"))
  )

  def dataChapters(chapters: List[Chapter]) = new TestChapters[IO]:
    override def findByAssetId(ids: List[AssetId]): IO[List[Chapter]] =
      chapters.pure

  extension (chapter: Chapter)
    def toCreate: CreateChapter =
      CreateChapter(
        sampleChapter.no,
        sampleChapter.url,
        sampleChapter.dateReleased,
        sampleChapter.assetId
      )
