package tui

import cats.Applicative
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.elements.PromptableElementIF
import scala.jdk.CollectionConverters.*
import de.codeshelf.consoleui.prompt.InputResult

package object views {
  private def getListPromptResult(rawPromptResult: PromtResultItemIF): String =
    rawPromptResult.asInstanceOf[ListResult].getSelectedId()

  private def getInputPromptResult(rawPromptResult: PromtResultItemIF): String =
    rawPromptResult.asInstanceOf[InputResult].getInput()

  private def showPrompt[F[_]: Applicative](
      prompt: ConsolePrompt,
      prompts: java.util.List[PromptableElementIF]
  ): F[scala.collection.mutable.Map[String, ? <: PromtResultItemIF]] =
    Applicative[F].pure {
      prompt.prompt(prompts).asScala
    }
}
