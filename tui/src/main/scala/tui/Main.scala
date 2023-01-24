package tui

import cats._
import cats.effect._
import tui.views.CrawlResultsView
import org.fusesource.jansi.AnsiConsole;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import library.domain.asset._
import library.domain.chapter._
import java.util.UUID
import java.util.Date

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = registerConsole.flatMap { _ =>
    val prompt = new ConsolePrompt()

    val view = new CrawlResultsView[IO](prompt, assets)
    view.view().as(ExitCode.Success)
  }

  private def registerConsole: IO[Unit] = IO.pure {
    AnsiConsole.systemInstall()
  }

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
