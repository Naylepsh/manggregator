package tui.prompts.handlers

import scala.collection.mutable
import scala.util.Try

import cats.Monad
import cats.implicits._
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt.builder.{ListPromptBuilder, PromptBuilder}
import de.codeshelf.consoleui.prompt.{ListResult, PromtResultItemIF}

case class SinglePropHandler[F[_], A, Output](
    addToPrompt: (A) => A,
    check: String => Boolean,
    handle: String => F[Output]
)

class Handler[F[_]: Monad, A, Output](
    subHandlers: List[SinglePropHandler[F, A, Output]],
    extractResult: mutable.Map[String, ? <: PromtResultItemIF] => Option[
      String
    ],
    val combinePrompts: PromptBuilder => java.util.List[PromptableElementIF]
):
  def handle(
      rawResult: mutable.Map[String, ? <: PromtResultItemIF]
  ): F[Option[Output]] =
    extractResult(rawResult) match
      case None         => None.pure
      case Some(result) => walk(result, subHandlers)

  private def walk(
      result: String,
      subHandlersLeft: List[SinglePropHandler[F, A, Output]]
  ): F[Option[Output]] =
    subHandlersLeft match
      case handler :: next =>
        if handler.check(result) then handler.handle(result).map(_.some)
        else walk(result, next)
      case Nil => None.pure

def makeListHandler[F[_]: Monad, B](
    subHandlers: List[SinglePropHandler[F, ListPromptBuilder, B]],
    promptName: String,
    promptMessage: String
): Handler[F, ListPromptBuilder, B] = new Handler(
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
