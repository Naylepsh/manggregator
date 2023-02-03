package tui

import scala.jdk.CollectionConverters.*

import cats.Applicative
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt._
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder

package object views {
  private def getListPromptResult(rawPromptResult: PromtResultItemIF): String =
    rawPromptResult.asInstanceOf[ListResult].getSelectedId()

  private def showPrompt[F[_]: Applicative](
      prompt: ConsolePrompt,
      prompts: java.util.List[PromptableElementIF]
  ): F[scala.collection.mutable.Map[String, ? <: PromtResultItemIF]] =
    Applicative[F].pure {
      prompt.prompt(prompts).asScala
    }
}
