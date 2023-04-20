package library.services

import java.time.Instant
import java.util.{Date, UUID}

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import library.domain.asset.*
import library.domain.chapter.*
import library.persistence

import common.*

class ChaptersSuite extends munit.FunSuite:
  import ChaptersSuite.*

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
      .map {
        case CreateChaptersResult(created, alreadyExist) =>
          assertEquals(created.length, newChapters.length)
          assertEquals(alreadyExist.length, 1)
      }
  }

object ChaptersSuite:
  val sampleChapter = Chapter(
    ChapterId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28c")),
    ChapterNo("42"),
    ChapterUrl("http://foo.bar/asset/42"),
    DateReleased(
      Date.from(Instant.parse("2022-11-06T12:52:54.966933505Z"))
    ),
    Seen(false),
    AssetId(UUID.fromString("0aed43c4-9be6-4d86-b418-dd0844d5a28b"))
  )

  def dataChapters(chapters: List[Chapter]) = new TestChapters[IO]:
    override def findByAssetIds(ids: List[AssetId]): IO[List[Chapter]] =
      chapters.pure

  extension (chapter: Chapter)
    def toCreate: CreateChapter =
      CreateChapter(
        sampleChapter.no,
        sampleChapter.url,
        sampleChapter.dateReleased,
        sampleChapter.assetId
      )
