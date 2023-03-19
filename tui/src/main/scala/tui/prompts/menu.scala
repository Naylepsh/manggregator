package tui.prompts

import cats.effect.kernel.Sync
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import tui.prompts.handlers.{SinglePropHandler, exitHandler, goBackHandler}
import tui.prompts.list.{Item, createItemsSubHandler, runOnInputFromList}
import tui.views.View

object menu:
  case class Action[F[_]](text: String, handle: String => F[Unit])
  type Actions[F[_]] = List[(String, Action[F])]
  object Actions:
    def get[F[_]](actions: Actions[F], key: String): Option[Action[F]] =
      actions.find(_._1 == key).map(_._2)

  def make[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      actions: Map[String, Action[F]],
      viewToGoBackTo: View[F]
  ): F[Unit] =
    val actionsHandler = createItemsSubHandler(
      actions.map { case (key, action) =>
        Item(id = key, text = action.text)
      }.toList,
      (result) =>
        actions
          .get(result)
          .map(_.handle(result))
          .getOrElse(Sync[F].unit)
    )
    make(prompt, message, actionsHandler, viewToGoBackTo)

  def make[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      actions: Actions[F],
      viewToGoBackTo: View[F]
  ): F[Unit] =
    val actionsHandler = createItemsSubHandler(
      actions.map { case (key, action) =>
        Item(id = key, text = action.text)
      },
      (result) =>
        Actions
          .get(actions, result)
          .map(_.handle(result))
          .getOrElse(Sync[F].unit)
    )
    make(prompt, message, actionsHandler, viewToGoBackTo)

  private def make[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      actionsHandler: SinglePropHandler[F, ListPromptBuilder, Unit],
      viewToGoBackTo: View[F]
  ): F[Unit] =
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
