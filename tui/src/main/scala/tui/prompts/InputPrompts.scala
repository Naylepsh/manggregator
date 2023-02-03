package tui.prompts

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.{ConsolePrompt, InputResult, PromtResultItemIF}

object InputPrompts:
  def getValidatedInput[F[_]: Sync: Console, A](
      prompt: ConsolePrompt,
      message: String,
      validate: String => Either[String, A]
  ) =
    getInput(prompt, message).flatMap { input =>
      validate(input) match
        case Left(reason) => Console[F].println(reason) *> reason.asLeft.pure
        case Right(value) => value.asRight[Throwable].pure
    }

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
