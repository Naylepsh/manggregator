package tui.prompts

import cats.effect.kernel.Sync
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import tui.prompts.handlers.{exitHandler, goBackHandler}
import tui.prompts.list.{Item, createItemsSubHandler, runOnInputFromList}
import tui.views.View

object menu:
  case class Action[F[_]](text: String, handle: String => F[Unit])

  def make[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      actions: Map[String, Action[F]],
      viewToGoBackTo: View[F]
  ) =
    val actionsHandler = createItemsSubHandler(
      actions.map { case (key, action) =>
        Item(id = key, text = action.text)
      }.toList,
      (result) =>
        actions.get(result).map(_.handle(result)).getOrElse(Sync[F].unit),
    )
    val handlers =
      List(
        actionsHandler,
        goBackHandler(viewToGoBackTo),
        exitHandler()
      )

    runOnInputFromList(
      prompt,
      message,
      handlers
    )
