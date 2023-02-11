package tui.views

import scala.jdk.CollectionConverters.*

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.{
  ConsolePrompt,
  ListResult,
  PromtResultItemIF
}
import library.domain.asset.AssetSummary
import tui.prompts.asset.makeAssetNameMenu
import tui.prompts.menu

class CrawlResultsView[F[_]: Sync: Console](
    context: Context[F],
    assets: List[AssetSummary],
    previous: View[F]
) extends View[F]:

  def view(): F[Unit] =
    menu.make(
      context.prompt,
      "Select an asset to see recent releases of:",
      actions,
      previous
    )

  private val actions = assets
    .map(summary =>
      summary.asset.id.value.toString -> menu
        .Action(
          text = summary.asset.name.value,
          handle = _ => showAssetChapters(summary) >> view()
        )
    )
    .toMap

  private def showAssetChapters(summary: AssetSummary): F[Unit] =
    summary.chapters.foldLeft(Applicative[F].unit) { (acc, chapter) =>
      acc *> Console[F].println(s"${chapter.no.value} | ${chapter.url.value}")
    }
