package tui.prompts

import de.codeshelf.consoleui.prompt.ConsolePrompt
import cats.effect.kernel.Sync
import cats.implicits._
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import de.codeshelf.consoleui.prompt.InputResult

object InputPrompts:
  def getInput[F[_]: Sync](prompt: ConsolePrompt, message: String): F[String] =
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

  private def getInputPromptResult(rawPromptResult: PromtResultItemIF): String =
    rawPromptResult.asInstanceOf[InputResult].getInput()
