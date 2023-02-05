package tui.prompts.handlers

import cats.Applicative
import cats.implicits._
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import tui.views.View

private val backId = "back"
def goBackHandler[F[_]](
    viewToGoBackTo: View[F]
) =
  SinglePropHandler[F, ListPromptBuilder, Unit](
    addToPrompt =
      (promptBuilder) => promptBuilder.newItem(backId).text(backId).add(),
    check = (result) => result == backId,
    handle = (_) => viewToGoBackTo.view()
  )

private val exitId = "exit"
def exitHandler[F[_]: Applicative]() =
  SinglePropHandler[F, ListPromptBuilder, Unit](
    addToPrompt =
      (promptBuilder) => promptBuilder.newItem(exitId).text(exitId).add(),
    check = (result) => result == exitId,
    handle = (_) => Applicative[F].unit
  )
