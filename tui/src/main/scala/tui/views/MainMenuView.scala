package tui.views

import cats.effect.std.Console
import cats.effect.kernel.Sync
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import scala.jdk.CollectionConverters.*
import cats.Applicative
import tui.actions.LibraryActions

class MainMenuView[F[_]: Console: Sync](
    prompt: ConsolePrompt
) extends View[F]:

  // TODO: Handle unsafe `get`s
  override def view(): F[Unit] =
    for
      rawResult <- showPrompt(prompt, buildActionsPrompt)
      actionId = getListPromptResult(rawResult.get(menuResultsName).get)
      action = actions.get(actionId).get
      _ <- action.onSelect()
    yield ()

  private val menuResultsName = "menu"

  private case class Action(text: String, onSelect: () => F[Unit])
  private val actions = Map(
    "crawl" -> Action(text = "Trigger a crawl", onSelect = triggerCrawl),
    "recent-releases" -> Action(
      text = "Browse recent releases",
      onSelect = browseRecentReleases
    ),
    "exit" -> Action(text = "exit", onSelect = () => Applicative[F].unit)
  )

  private def triggerCrawl(): F[Unit] = Applicative[F].unit

  private def browseRecentReleases(): F[Unit] =
    for
      minDate <- getInput("Enter yoru input:")
      assets <- LibraryActions.getRecentReleases()
      _ <- new CrawlResultsView[F](prompt, assets).view()
    yield ()

  private def buildActionsPrompt =
    val promptBuilder = prompt.getPromptBuilder()

    val header = promptBuilder
      .createListPrompt()
      .name(menuResultsName)
      .message("Pick an action:")

    actions
      .foldLeft(header) { (builder, item) =>
        val id = item._1
        val action = item._2
        builder.newItem(id).text(action.text).add()
      }
      .addPrompt()
      .build()

  private def getInput(message: String): F[String] =
    val promptBuilder = prompt.getPromptBuilder()
    val promptId = "input"
    val inputPrompt = promptBuilder
      .createInputPrompt()
      .name(promptId)
      .message(message)
      .addPrompt()
      .build()

    Sync[F].pure(prompt.prompt(inputPrompt)).map { rawResult =>
      getInputPromptResult(rawResult.get(promptId))
    }
