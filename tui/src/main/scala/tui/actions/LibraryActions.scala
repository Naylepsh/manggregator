package tui.actions

import cats._
import cats.effect._
import tui.views.CrawlResultsView
import org.fusesource.jansi.AnsiConsole;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import java.util.UUID
import java.util.Date
import library.domain.asset._
import library.domain.chapter._

object LibraryActions:
  val assets = List(
    Asset(
      id = AssetId(UUID.randomUUID()),
      name = AssetName("foo"),
      enabled = Enabled(true),
      List(),
      chapters = List(
        Chapter(
          id = ChapterId(UUID.randomUUID()),
          no = ChapterNo("1"),
          url = ChapterUrl("https://site.com/foo/1"),
          dateReleased = DateReleased(new Date()),
          assetId = AssetId(UUID.randomUUID())
        ),
        Chapter(
          id = ChapterId(UUID.randomUUID()),
          no = ChapterNo("2"),
          url = ChapterUrl("https://site.com/foo/2"),
          dateReleased = DateReleased(new Date()),
          assetId = AssetId(UUID.randomUUID())
        )
      )
    ),
    Asset(
      id = AssetId(UUID.randomUUID()),
      name = AssetName("bar"),
      enabled = Enabled(true),
      List(),
      chapters = List(
        Chapter(
          id = ChapterId(UUID.randomUUID()),
          no = ChapterNo("42"),
          url = ChapterUrl("https://site.com/bar/42"),
          dateReleased = DateReleased(new Date()),
          assetId = AssetId(UUID.randomUUID())
        ),
        Chapter(
          id = ChapterId(UUID.randomUUID()),
          no = ChapterNo("42.5"),
          url = ChapterUrl("https://site.com/bar/42-5"),
          dateReleased = DateReleased(new Date()),
          assetId = AssetId(UUID.randomUUID())
        )
      )
    )
  )

  def getRecentReleases[F[_]: Applicative](): F[List[Asset]] =
    Applicative[F].pure(assets)
