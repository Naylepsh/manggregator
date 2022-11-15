package library.services

import library.domain.chapter._
import library.domain.asset._
import library.persistence
import java.util.UUID
import java.util.Date
import java.time.Instant
import cats._

class ChaptersSuite extends munit.FunSuite:
  import ChaptersSuite._

  test("Cant create the same chapter twice") {
    val chapters = (1 to 3)
      .map(i =>
        CreateChapter(
          ChapterNo(i.toString),
          ChapterUrl(s"http://foo.bar/asset/${i}"),
          DateReleased(
            Date.from(Instant.parse("2022-11-06T12:52:54.966933505Z"))
          ),
          AssetId(uuids.head)
        )
      )
      .toList

    val chapterStore = new persistence.Chapters[Id]:
      override def create(chapters: List[CreateChapter]): Id[List[ChapterId]] =
        List()
      override def findByAssetId(ids: List[AssetId]): Id[List[Chapter]] =
        chapters.zip(uuids).map { case (chapter, id) =>
          Chapter(
            ChapterId(id),
            chapter.no,
            chapter.url,
            chapter.dateReleased,
            chapter.assetId
          )
        }

    val result = Chapters.make(chapterStore).create(chapters)
    result.map(ids => assertEquals(ids.length, 0))
  }

object ChaptersSuite:
  val uuids = List(
    "0aed43c4-9be6-4d86-b418-dd0844d5a28a",
    "0aed43c4-9be6-4d86-b418-dd0844d5a28b",
    "0aed43c4-9be6-4d86-b418-dd0844d5a28c"
  ).map(UUID.fromString)
