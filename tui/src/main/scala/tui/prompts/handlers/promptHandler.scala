package tui.prompts.handlers

import de.codeshelf.consoleui.prompt.PromtResultItemIF
import cats.Monad
import cats.implicits._
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.ListResult
import scala.util.Try
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt.builder.PromptBuilder
import scala.collection.mutable

case class SinglePropHandler[F[_], A](
    addToPrompt: (A) => A,
    check: String => Boolean,
    handle: String => F[Unit]
)

class Handler[F[_]: Monad, A](
    subHandlers: List[SinglePropHandler[F, A]],
    extractResult: mutable.Map[String, ? <: PromtResultItemIF] => Option[
      String
    ],
    val combinePrompts: PromptBuilder => java.util.List[PromptableElementIF]
):
  def handle(
      rawResult: mutable.Map[String, ? <: PromtResultItemIF]
  ): F[Option[Unit]] =
    extractResult(rawResult) match
      case None         => None.pure
      case Some(result) => walk(result, subHandlers)

  private def walk(
      result: String,
      subHandlersLeft: List[SinglePropHandler[F, A]]
  ): F[Option[Unit]] =
    subHandlersLeft match
      case handler :: next =>
        if handler.check(result) then handler.handle(result).map(_.some)
        else walk(result, next)
      case Nil => None.pure

def makeListHandler[F[_]: Monad](
    subHandlers: List[SinglePropHandler[F, ListPromptBuilder]],
    promptName: String,
    promptMessage: String
): Handler[F, ListPromptBuilder] = new Handler(
  subHandlers,
  (rawResult) =>
    rawResult.get(promptName).flatMap { value =>
      Try(value.asInstanceOf[ListResult].getSelectedId()).toOption
    },
  (promptBuilder) =>
    val listBuilder = promptBuilder
      .createListPrompt()
      .name(promptName)
      .message(promptMessage)

    subHandlers
      .foldLeft(listBuilder)((builder, handler) => handler.addToPrompt(builder))
      .addPrompt()
      .build()
)
