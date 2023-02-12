package tui

import scala.jdk.CollectionConverters.*

import cats.Applicative
import crawler.domain.Library
import crawler.services.Crawler
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.{ConsolePrompt, _}
import library.services.{Assets, Chapters, Pages}

package object views {
  case class Services[F[_]](
      assets: Assets[F],
      pages: Pages[F],
      chapters: Chapters[F],
      crawler: Crawler[F],
      crawlerLibrary: Library[F]
  )

  case class Context[F[_]](
      prompt: ConsolePrompt,
      services: Services[F]
  )

  private def getListPromptResult(rawPromptResult: PromtResultItemIF): String =
    rawPromptResult.asInstanceOf[ListResult].getSelectedId()

  def showPrompt[F[_]: Applicative](
      prompt: ConsolePrompt,
      prompts: java.util.List[PromptableElementIF]
  ): F[scala.collection.mutable.Map[String, ? <: PromtResultItemIF]] =
    Applicative[F].pure {
      prompt.prompt(prompts).asScala
    }
}
