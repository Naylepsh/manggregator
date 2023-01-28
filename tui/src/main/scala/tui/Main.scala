package tui

import cats._
import cats.effect._
import tui.views.CrawlResultsView
import org.fusesource.jansi.AnsiConsole;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import tui.views.MainMenuView

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = registerConsole.flatMap { _ =>
    val prompt = new ConsolePrompt()

    val view = new MainMenuView[IO](prompt)
    view.view().as(ExitCode.Success)
  }

  private def registerConsole: IO[Unit] = IO.pure {
    AnsiConsole.systemInstall()
  }
